//
//  MusicController.m
//  Prism Lights
//
//  Created by Alma Knudson on 2/19/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "MusicController.h"
#import <HueSDK_iOS/HueSDK.h>

//#import "Prism Lights-Bridging-Header.h"

@interface MusicController(){
    COMPLEX_SPLIT _A;
    FFTSetup      _FFTSetup;
    BOOL          _isFFTSetup;
    vDSP_Length   _log2n;
}




@property (weak, nonatomic) IBOutlet UILabel *maxBrightnessLabel;

@property (weak, nonatomic) IBOutlet UILabel *bpmLabel;
@property (weak, nonatomic) IBOutlet UILabel *lowRangeMax;
@property (weak, nonatomic) IBOutlet UILabel *midRangeMin;
@property (weak, nonatomic) IBOutlet UILabel *midRangeMax;
@property (weak, nonatomic) IBOutlet UISlider *lowFrequencyRangeSlider;
@property (weak, nonatomic) IBOutlet UISlider *midFrequencyRangeSlider;
@property (weak, nonatomic) IBOutlet UILabel *highRangeMin;
@property (weak, nonatomic) IBOutlet UISlider *highFrequencyRangeSlider;

@end


@implementation MusicController
@synthesize audioPlotFreq;
@synthesize audioPlotTime;
@synthesize microphone;



//@property (weak) IBOutlet UILabel *bpmLabel;
- (IBAction)lowFrequencyRangeSlider:(UISlider *)sender{
    lowFrequencyRangeMax = sender.value;
    _lowRangeMax.text = [NSString stringWithFormat:@"%dhz", (int)sender.value];
    _midRangeMin.text = [NSString stringWithFormat:@"%dhz", (int)sender.value + 1];
    _midFrequencyRangeSlider.minimumValue = (int)sender.value + 1;
    //    slider.minimumValue = (int)sender.value;
    //    slider.maximumValue = 3.0f;
}

- (IBAction)midFrequencyRangeSlider:(UISlider *)sender {
    midFrequencyRangeMax = sender.value;
    
    if((int)_lowFrequencyRangeSlider.value >= sender.value){
        _lowFrequencyRangeSlider.value = (int)sender.value - 1;
        _lowRangeMax.text = [NSString stringWithFormat:@"%dhz", (int)sender.value - 1];
    }
    _midRangeMax.text = [NSString stringWithFormat:@"%dhz", (int)sender.value];
    _highRangeMin.text = [NSString stringWithFormat:@"%dhz", (int)sender.value + 1];
    
}

- (IBAction)highFrequencyRangeSlider:(UISlider *)sender {
    
    
}



- (IBAction)startListening :(UISlider *)sender{
    UISwitch *mySwitch = (UISwitch *)sender;
    if([mySwitch isOn])
    {
        
        lowTimer = [NSTimer scheduledTimerWithTimeInterval:sensitivity
                                                    target:self
                                                  selector:@selector(updateLowTimerFlag:)
                                                  userInfo:nil
                                                   repeats:YES];
        midTimer = [NSTimer scheduledTimerWithTimeInterval:sensitivity
                                                    target:self
                                                  selector:@selector(updateMidTimerFlag:)
                                                  userInfo:nil
                                                   repeats:YES];
        
        highTimer = [NSTimer scheduledTimerWithTimeInterval:sensitivity
                                                     target:self
                                                   selector:@selector(updateHighTimerFlag:)
                                                   userInfo:nil
                                                    repeats:YES];
        
        NSTimer *decrementTimer = [NSTimer scheduledTimerWithTimeInterval:1.0
                                                                   target:self
                                                                 selector:@selector(resetHits:)
                                                                 userInfo:nil
                                                                  repeats:YES];
        
        
        /*
         Customizing the audio plot's look
         */
        // Setup time domain audio plot
        self.audioPlotTime.backgroundColor = [UIColor colorWithRed: 0.0904 green: 0.0901 blue: 0.105 alpha: 1];
        self.audioPlotTime.color           = [UIColor colorWithRed:1.0 green:1.0 blue:1.0 alpha:1.0];
        self.audioPlotTime.shouldFill      = YES;
        self.audioPlotTime.shouldMirror    = YES;
        self.audioPlotTime.plotType        = EZPlotTypeRolling;
        
        // Setup frequency domain audio plot
        //    self.audioPlotFreq.backgroundColor = [UIColor colorWithRed: 0.114 green: 0.111 blue: 0.125 alpha: 1];
        //    self.audioPlotFreq.color           = [UIColor colorWithRed:1.0 green:1.0 blue:1.0 alpha:1.0];
        //    self.audioPlotFreq.shouldFill      = YES;
        //    self.audioPlotFreq.plotType        = EZPlotTypeBuffer;
        
        /*
         Start the microphone
         */
        self.microphone = [EZMicrophone microphoneWithDelegate:self
                                             startsImmediately:YES];
        
        
        bridgeSendAPI = [[PHBridgeSendAPI alloc] init];
        
        //Range selection code:
//        PHBridgeResourcesCache *cache = [PHBridgeResourcesReader readBridgeResourcesCache];
//        if (cache != nil && cache.bridgeConfiguration != nil && cache.bridgeConfiguration.ipaddress != nil){
//            NSArray *myLights = [cache.lights allValues];
//            if(musicCells != nil) {
//                for (PHLight *light in myLights) {
//                    for(MusicCell *cell in musicCells){
////                        if (light.identifier ==)
//                        if([light.identifier isEqualToString:cell.bulbIdentifier]){
//                            
//                        }
//                    }
//                }
//                
//            }
//        }
        
    } else {
        //Clean up.
        [lowTimer invalidate];
        [midTimer invalidate];
        [highTimer invalidate];
        self.audioPlotTime.backgroundColor = [UIColor colorWithRed: 0.0904 green: 0.0901 blue: 0.105 alpha: 1];
        
        //        self.audioPlotTime.backgroundColor = nil;
        //        self.audioPlotTime.color           = nil;
        //        self.audioPlotTime.shouldFill      = nil;
        //        self.audioPlotTime.shouldMirror    = nil;
        //        self.audioPlotTime.plotType        = nil;
        self.microphone.stopFetchingAudio;
        
        
    }
    
}




- (IBAction)maxBrightnessSliderValueChanged :(UISlider *)sender{
    maxBrightness = sender.value;
    NSString* formattedNumber = [NSString stringWithFormat:@"%i%@", (int) ((((float)1.0*sender.value)/254.0)*100), @"%"];
    _maxBrightnessLabel.text = formattedNumber;
    NSLog(@"maxBrightess:%i", maxBrightness);
}


//This is Beats Per Minute Slide Handler:
- (IBAction)sensitivitySliderValueChanged :(UISlider *)sender{
    //    label.text = [NSString stringWithFormat:@"%f", sender.value];
    //Sender.value between [0-1]... 0 being SLOW 1 being FAST/SENSITIVE
    float b = 1.33;
    float M = 0.66/120;
    float y = b - (M * sender.value);
    NSLog(@"%f", y);
    _bpmLabel.text = [NSString stringWithFormat:@"%d",(int)sender.value];
    sensitivity = y;
    
    [lowTimer invalidate];
    [midTimer invalidate];
    [highTimer invalidate];
    
    lowTimer = [NSTimer scheduledTimerWithTimeInterval:sensitivity
                                                target:self
                                              selector:@selector(updateLowTimerFlag:)
                                              userInfo:nil
                                               repeats:YES];
    midTimer = [NSTimer scheduledTimerWithTimeInterval:sensitivity
                                                target:self
                                              selector:@selector(updateMidTimerFlag:)
                                              userInfo:nil
                                               repeats:YES];
    
    highTimer = [NSTimer scheduledTimerWithTimeInterval:sensitivity
                                                 target:self
                                               selector:@selector(updateHighTimerFlag:)
                                               userInfo:nil
                                                repeats:YES];
    
}




//Counter
int counter;
float min = 500;
float max = 500;
int currentRangeBin;
float runningMinSound = 0;
float runningMaxSound = 1;

float runningMaxMag = 0;

bool sendChange;

//["RED":65280, "YELLOW":12950, "WHITE":36210, "BLUE":46920, "PURPLE":56100, "PINK":53505, "ORANGE":10000, "GREEN": 25500];

NSInteger highs[3] = {30000, 30000, 30000};
NSInteger mids[3] = {0, 0, 0};
NSInteger lows[3] = {46000, 46000, 46000};
NSTimer *lowTimer;
NSTimer *midTimer;
NSTimer *highTimer;
NSTimer *overloadedHitsTimer;

//NSDictionary *musicCells = [NSDictionary dictionaryWithObject:MusicCell forKeys:]
NSArray *musicCells;
MusicCell *allBulbs[3];



//Sensitivity is how often can a bulb change... 1 means once per second. .25 means 4 times per second and 5 means 1 time in every 5 seconds.

int lowFrequencyRangeMax = 1500;
int midFrequencyRangeMax = 2700;
int maxBrightness = 254;
float sensitivity = 1;
float previousLoudness = 100000;
bool canSend = true;
float lastHigh = 0;
float lastMid = 0;
float lastLow = 0;
int lastHighHue = 30000;
int lastMidHue = 0;
int lastLowHue = 46000;
int highsIndex = 0;
int midsIndex = 0;
int lowsIndex = 0;
bool highsFlag = false;
bool midsFlag = false;
bool lowsFlag = false;
PHBridgeSendAPI *bridgeSendAPI;


int maxHitsPerSecond = 10;
int currentHitsPerSecond = 0;
bool throttleHits = false;
bool throttleSkip = true;



-(void)setBulb:(NSString*)identifier withSaturation:(NSNumber*)saturation withBrightness:(NSNumber*)brightness withHue:(NSNumber*)hue
{
    if(currentHitsPerSecond > maxHitsPerSecond){
        //        NSLog(@"OVERLOADED");
        throttleHits = true;
        overloadedHitsTimer = [NSTimer scheduledTimerWithTimeInterval:1
                                                               target:self
                                                             selector:@selector(resetThrottling:)
                                                             userInfo:nil
                                                              repeats:YES];
        return;
    }
    
    //If we are overloading the bridge and the bulbs then we will need to throttle the output.  Hopefully this doesn't hurt experience.
    if(throttleHits){
        if(!throttleSkip){
            throttleHits = true;
            return;
        }
        throttleHits = false;
    }
    
    
    PHBridgeResourcesCache *cache = [PHBridgeResourcesReader readBridgeResourcesCache];
    if (cache != nil && cache.bridgeConfiguration != nil && cache.bridgeConfiguration.ipaddress != nil){
        NSArray *myLights = [cache.lights allValues];
        //    for (PHLight *light in myLights) {
        //        NSLog(@"LOWS-->Brightness: %f \t Color: %li \t frequency: %f", intensity, (long)hueVal, frequency);
        // do something with object
        //Get light from cache
        
        for (PHLight *light in myLights) {
            
            if([light.identifier isEqualToString:identifier]){
                
                
                //Get Light state of this light
                //               PHLightState *state = light.lightState;
                
                
                PHLightState *lightState = [[PHLightState alloc] init];
                if(![light.lightState.on  isEqual: @YES] )
                    lightState.on = @YES;
                //Change hue of this light state
                lightState.hue = hue;
                lightState.saturation = saturation;
                lightState.transitionTime = [NSNumber numberWithInt:.5];
                //    state.brightness = [NSNumber numberWithFloat:intensity];
                lightState.brightness = brightness;
                
                
                PHBridgeSendAPI *bridgeSendAPI = [[PHBridgeSendAPI alloc] init];
                
                [bridgeSendAPI updateLightStateForId:identifier withLightState:lightState completionHandler:nil];
            }
        }
    }
}


- (void)updateHighBulbs:(int)intensity withFrequency:(float)frequency
{
    //    if(fabsf( (frequency-lastHigh) ) < 10)
    //        if(!highsFlag)
    //            return;
    if(highsFlag){
        NSInteger temp = highs[highsIndex];
        if(highsIndex + 1 > 3){
            highsIndex = 0;
        } else{
            highsIndex = highsIndex + 1;
        }
        int r = arc4random() % 18180 + temp;
        while(fabs((double) r - lastHighHue) < 2000){
            r = arc4random() % 18180 + temp;
        }
        lastHighHue = r;
        NSInteger hueVal = r;
        NSNumber *b = [NSNumber numberWithInteger:intensity];
        
        //        NSLog(@"HIGHS-->Brightness: %f \t Color: %li \t frequency: %f", intensity, (long)hueVal, frequency);
        [self setBulb:@"1" withSaturation:@254 withBrightness:b withHue:[NSNumber numberWithInteger:(NSInteger) hueVal]];
    }
    highsFlag = false;
}

- (void)updateMidBulbs:(int)intensity withFrequency:(float)frequency
{
    //    if(fabsf( (frequency-lastMid) ) < 10)
    //        if(!midsFlag)
    //            return;
    if(midsFlag)
    {
        NSInteger temp = mids[midsIndex];
        if(midsIndex + 1 > 3){
            midsIndex = 0;
        } else{
            midsIndex = midsIndex + 1;
        }
        int r = arc4random() % 18180 + temp;
        while(fabs((double) r - lastMidHue) < 2000){
            r = arc4random() % 18180 + temp;
        }
        lastMidHue = r;
        NSInteger hueVal = r;
        NSNumber *b = [NSNumber numberWithInteger:intensity];
        
        //        NSLog(@"MIDS-->Brightness: %f \t Color: %li \t frequency: %f", intensity, (long)hueVal, frequency);
        [self setBulb:@"2" withSaturation:@254 withBrightness:b withHue:[NSNumber numberWithInteger:(NSInteger) hueVal]];
    }
    midsFlag = false;
}

- (void)updateLowBulbs:(int)intensity withFrequency:(float)frequency
{
    //    if(fabsf( (frequency-lastLow) ) < 10)
    //    {
    //        if(!lowsFlag)
    //            return;
    //    }
    if(lowsFlag)
    {
        NSInteger temp = lows[lowsIndex];
        if(lowsIndex + 1 > 3){
            lowsIndex = 0;
        } else{
            lowsIndex = lowsIndex + 1;
        }
        int r = arc4random() % 18180 + temp;
        while(fabs((double) r - lastLowHue) < 2000){
            r = arc4random() % 18180 + temp;
        }
        lastLowHue = r;
        NSInteger hueVal = r;
        //        NSLog(@"LOWS-->Brightness: %f \t Color: %li \t frequency: %f", intensity, (long)hueVal, frequency);
        bool f = (((int)intensity % 2) == 0);
        if(f)
            [self setBulb:@"3" withSaturation:@254 withBrightness:[NSNumber numberWithInteger:intensity] withHue:[NSNumber numberWithInteger:(NSInteger) hueVal]];
        else
            [self setBulb:@"3" withSaturation:@254 withBrightness:@0 withHue:[NSNumber numberWithInteger:(NSInteger) hueVal]];
    }
    lowsFlag = false;
}

-(void)updateLowTimerFlag:(NSTimer *)timer
{
    lowsFlag = true;
    currentHitsPerSecond ++;
}


-(void)updateHighTimerFlag:(NSTimer *)timer
{
    highsFlag = true;
    currentHitsPerSecond ++;
}

-(void)updateMidTimerFlag:(NSTimer *)timer
{
    midsFlag = true;
    currentHitsPerSecond ++;
}

-(void)resetHits:(NSTimer *)timer
{
    currentHitsPerSecond = 0;
}


-(void)resetThrottling:(NSTimer *)timer
{
    throttleHits = false;
    [overloadedHitsTimer invalidate];
}


#pragma mark - Customize the Audio Plot
- (void)viewDidLoad
{
    [super viewDidLoad];
    self.audioPlotTime.backgroundColor = [UIColor colorWithRed: 0.0904 green: 0.0901 blue: 0.105 alpha: 1];
    
    
    //TODO--REFACTOR what needs to be here.
    //    BulbRangeSelectionDelegate *selectBulbs = [[[self.childViewControllers lastObject] ] bulbRangeSelectionDelegate];
    //    selectBulbs.BulbRangeSelectionDelegate = self;
    //
    
    //    selectBulbs.
    //    selectBulb
    //    var picker = ((self.childViewControllers.last)?.view) as ColorPicker
    //    picker.colorChangedDelegate = self
    
}

#pragma mark - FFT
/**
 Adapted from http://batmobile.blogs.ilrt.org/fourier-transforms-on-an-iphone/
 */
-(void)createFFTWithBufferSize:(float)bufferSize withAudioData:(float*)data {
    
    // Setup the length
    _log2n = log2f(bufferSize);
    
    // Calculate the weights array. This is a one-off operation.
    _FFTSetup = vDSP_create_fftsetup(_log2n, FFT_RADIX2);
    
    // For an FFT, numSamples must be a power of 2, i.e. is always even
    int nOver2 = bufferSize/2;
    
    // Populate *window with the values for a hamming window function
    float *window = (float *)malloc(sizeof(float)*bufferSize);
    vDSP_hamm_window(window, bufferSize, 0);
    // Window the samples
    vDSP_vmul(data, 1, window, 1, data, 1, bufferSize);
    free(window);
    
    // Define complex buffer
    _A.realp = (float *) malloc(nOver2*sizeof(float));
    _A.imagp = (float *) malloc(nOver2*sizeof(float));
    
}

-(void)updateFFTWithBufferSize:(float)bufferSize withAudioData:(float*)data {
    //    if(counter > 3){
    //        counter = 0;
    
    
    // For an FFT, numSamples must be a power of 2, i.e. is always even
    int nOver2 = bufferSize/2;
    
    // Pack samples:
    // C(re) -> A[n], C(im) -> A[n+1]
    vDSP_ctoz((COMPLEX*)data, 2, &_A, 1, nOver2);
    
    // Perform a forward FFT using fftSetup and A
    // Results are returned in A
    vDSP_fft_zrip(_FFTSetup, &_A, 1, _log2n, FFT_FORWARD);
    
    // Convert COMPLEX_SPLIT A result to magnitudes
    float amp[nOver2]; //256
    float maxMag = 0;
    float frequency = 0;
    float runningFrequency = 0;
    int lhs = nOver2/3;
    int rhs = lhs*2;
    
    int index = 0;
    int frequencyCount = 1;
    float frequencyTotal = 0;
    
    float lMaxMag = 0;
    int lMaxMagIndex = 0;
    float mMaxMag = 0;
    int mMaxMagIndex = 0;
    float hMaxMag = 0;
    int hMaxMagIndex = 0;
    //    int lowIndex = 0;
    //    int midIndex = 0;
    //    int highIndex = 0;
    
    int maxIndex = 0;
    
    for(int i=0; i<nOver2; i++) {
        // Calculate the magnitude
        float mag = _A.realp[i]*_A.realp[i]+_A.imagp[i]*_A.imagp[i];
        if(i<lhs){
            if(mag > lMaxMag)
            {
                lMaxMag = mag;
                lMaxMagIndex = i;
            }
        } else if(i < rhs){
            if(mag > mMaxMag)
            {
                mMaxMag = mag;
                mMaxMagIndex = i;
            }
        } else {
            if(mag > hMaxMag)
            {
                hMaxMag = mag;
                hMaxMagIndex = i;
            }
        }
        
        //THIS IS SOME RANDOM SHIT-->
        if(mag > maxMag) {
            maxMag = mag;
            index = i;
        }
        
        //        maxMag = mag > maxMag ? mag : maxMag;
        
        //END OF SOME RANDOM GOBBLYGOOP
    }
    if( (maxMag/previousLoudness) < .33){
        previousLoudness = maxMag;
        return;
    } else{
        previousLoudness = maxMag;
    }
    if(index > maxIndex){
        maxIndex = index;
    }
    
    if(maxMag > runningMaxMag)
        runningMaxMag = maxMag;
    
    if(runningMaxMag > 10)
        runningMaxMag = runningMaxMag > maxMag ? (runningMaxMag - 10)  : maxMag;
    else{
        runningMaxMag = runningMaxMag > maxMag ? (runningMaxMag)  : maxMag;
    }
    
    
    
    frequency = (double) index * (44100.0 / nOver2 / 2.0);
    
    if(frequencyCount < 10000){
        frequencyTotal = (frequencyTotal + frequency);
        runningFrequency = frequencyTotal/frequencyCount++;
    } else{
        frequencyCount = 1;
        frequencyTotal = 0;
        runningFrequency = frequency;
    }
    
    //    NSLog(@"%f", runningFrequency);
    
    for(int i=0; i<nOver2; i++) {
        // Calculate the magnitude
        float mag = _A.realp[i]*_A.realp[i]+_A.imagp[i]*_A.imagp[i];
        // Bind the value to be less than 1.0 to fit in the graph
        amp[i] = [EZAudio MAP:mag leftMin:0.0 leftMax:maxMag rightMin:0.0 rightMax:1.0];
    }
    
    
    
    //    public static double Index2Freq(int i, double samples, int nFFT) {
    //        return (double) i * (samples / nFFT / 2.);
    //    }
    
    float maxSound = data[0];
    for(int i = 0; i < bufferSize; i ++){
        maxSound = maxSound > data[i] ? maxSound  : data[i];
    }
    if(runningMaxSound >= 1)
        runningMaxSound = runningMaxSound > maxSound ? (runningMaxSound - .025)  : maxSound;
    else{
        runningMaxSound = runningMaxSound > maxSound ? (runningMaxSound)  : maxSound;
    }
    if(maxMag < 15)
        return;
    //    NSLog(@"%f", maxMag);
    
    
    /* Play with the frequency-->Determine high/mid/low*/
    //        if(maxMag > max){
    //            max = maxMag;
    //        }
    //            NSLog(@"%f", maxMag);
    //    NSLog(@"%f", runningMaxSound);
    //        if(maxMag < min){
    //            min = maxMag;
    //        } else {
    //            min = min;
    //        }
    // NSLog(@"%f \t %f", max, min);
    /* Plotting the time domain plot takes around 50-60% CPU... HOLY MOSES */
    
    //        int magScalar = 2;
    if(frequency < lowFrequencyRangeMax){
        currentRangeBin = 3;
    } else if(frequency < midFrequencyRangeMax){
        currentRangeBin = 2;
    } else {
        currentRangeBin = 1;
    }
    
    //        if(frequency < (2*runningFrequency/3)){
    //            currentRangeBin = 3;
    //        } else if(frequency < runningFrequency + (1*runningFrequency/3)){
    //            currentRangeBin = 2;
    //        } else {
    //            currentRangeBin = 1;
    //        }
    
    //    NSLog(@"%f", frequency);
    
    //    if(index < maxIndex/3){
    //        currentRangeBin = 3;
    //    } else if(index < (2*(maxIndex/3)) ){
    //        currentRangeBin = 2;
    //    } else {
    //        currentRangeBin = 1;
    //    }
    
    /* Play with the sound-->Determine whether or not to CHANGE bulbs */
    if( (maxSound < 0 && runningMaxSound > 0) || (maxSound > 0 && runningMaxSound < 0) )
        maxSound = maxSound * -1;
    //    NSLog(@"%f", (runningMaxSound / maxSound));
    
    //    NSLog(@"frequency: %f", frequency);
    //    return;
    
    
    //I think this is LOUD-->FORTISSIMO
    //            NSLog(@"HARD HIT");
    
    bool l = false;
    bool m = false;
    bool h = false;
    if( (lMaxMag > 5 && mMaxMag > 5) ){
        //                    NSLog(@"%f, %f, %f", lMaxMag, mMaxMag, hMaxMag);
        l = true;
        m = true;
    }
    if (  (lMaxMag > 5 && hMaxMag > 5)){
        l = true;
        h = true;
    }
    if (  (mMaxMag > 5 && hMaxMag > 5)){
        m = true;
        h = true;
    }
    
    
    int brightness = ((maxMag / runningMaxMag)*maxBrightness);
    
    
    //    brightness = max((double)brightness, (double)maxBrightness);
    
    //        NSLog(@"Brightness:%x", brightness);
    if(currentRangeBin == 1 || h){
        //SEND CHANGE TO BIN 1
        //                    NSLog(@"HIGH");
        //[self createFFTWithBufferSize:bufferSize withAudioData:buffer[0]];
        [self updateHighBulbs:brightness withFrequency:frequency];
    }
    if(currentRangeBin == 2 || m) {
        //SEND CHANGE TO BIN 2
        //                     NSLog(@"MID");
        [self updateMidBulbs:brightness withFrequency:frequency];
    }
    if(currentRangeBin == 3 || l){
        //SNED CHANGE TO BIN 3
        //                     NSLog(@"LOW");
        [self updateLowBulbs:brightness withFrequency:frequency];
    }
    
    
    
    /*
     else if( (runningMaxSound / maxSound) < 4 ){
     //This is medium loudness. meto forte
     //            NSLog(@"MEDIUM HIT");
     sendChange = true;
     } else if( (runningMaxSound / maxSound) < 6 ){
     //This is quiter.
     //            NSLog(@"SOFT HIT");
     sendChange = true;
     }
     */
    
    
    // NSLog(@"%f", maxSound);
    
    /* Plotting frequency domain + FFT --> 3% CPU and minimal memory footprint */
    // Update the frequency domain plot
    //    [self.audioPlotFreq updateBuffer:amp
    //                      withBufferSize:64];
    
    //
    //        // Update time domain plot
    //        [self.audioPlotTime updateBuffer:data
    //                          withBufferSize:bufferSize];
    //
    
    
    //    } else {
    //        counter = counter + 1;
    //    }
    //
    
    //We have the FFT results of the current audio data buffer:
    
    
}

#pragma mark - EZMicrophoneDelegate
-(void)    microphone:(EZMicrophone *)microphone
     hasAudioReceived:(float **)buffer
       withBufferSize:(UInt32)bufferSize
 withNumberOfChannels:(UInt32)numberOfChannels {
    dispatch_async(dispatch_get_main_queue(), ^{
        
        
        
        // Update time domain plot
        [self.audioPlotTime updateBuffer:buffer[0]
                          withBufferSize:bufferSize];
        
        
        //We have AUDIO DATA:
        
        
        
        // Setup the FFT if it's not already setup
        if( !_isFFTSetup ){
            [self createFFTWithBufferSize:bufferSize withAudioData:buffer[0]];
            _isFFTSetup = YES;
        }
        
        // Get the FFT data
        [self updateFFTWithBufferSize:bufferSize withAudioData:buffer[0]];
        
    });
}

-(void)onBulbRangeChange:(NSString *) identifier withRange: (int *)range {
    NSLog(@"SHIT YASS-BulbRangeChanged");
}

-(void)onBulbRangeChange:(MusicCell *) cell {
     NSLog(@"SHIT YASS-BulbRangeChangedzzzzzzzzzzzzzzz%@--%i", cell.bulbIdentifier, cell.bulbRange);
    
}

-(void)thisIsATest{
    NSLog(@"SHIT YASS-BulbRangeChanged--TEST");
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    if ([segue.identifier isEqualToString:@"musicLights"]) {
        NSLog(@"@WTF");
        //        NSIndexPath *indexPath = [self.tableView indexPathForSelectedRow];
        MusicSelectBulbsViewController *destViewController = (MusicSelectBulbsViewController *)segue.destinationViewController;
        //        destViewController.ligh
        destViewController.rangeSelectionDelegate = self;
    }
}



@end
