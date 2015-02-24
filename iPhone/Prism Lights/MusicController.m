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

@interface MusicController (){
    COMPLEX_SPLIT _A;
    FFTSetup      _FFTSetup;
    BOOL          _isFFTSetup;
    vDSP_Length   _log2n;
}
@end

@implementation MusicController
@synthesize audioPlotFreq;
@synthesize audioPlotTime;
@synthesize microphone;


//Counter
int counter;
float min = 500;
float max = 500;
int currentRangeBin;
float runningMinSound = 0;
float runningMaxSound = 1;

bool sendChange;

//["RED":65280, "YELLOW":12950, "WHITE":36210, "BLUE":46920, "PURPLE":56100, "PINK":53505, "ORANGE":10000, "GREEN": 25500];

NSInteger highs[3] = {65280, 36210, 53505};
NSInteger mids[3] = {46920, 56100, 51000};
NSInteger lows[3] = {12950, 25500, 10000};
int highsIndex = 0;
int midsIndex = 0;
int lowsIndex = 0;
bool highsFlag = false;
bool midsFlag = false;
bool lowsFlag = false;

- (void)updateHighBulbs:(float)intensity withFrequency:(float)frequency
{
    if(highsFlag){
    NSInteger hueVal = highs[highsIndex];
    if(highsIndex + 1 > 3){
        highsIndex = 0;
    } else{
        highsIndex = highsIndex + 1;
    }
    
    
    PHBridgeResourcesCache *cache = [PHBridgeResourcesReader readBridgeResourcesCache];
    NSArray *myLights = [cache.lights allValues];
//    for (PHLight *light in myLights) {
        NSLog(@"HIGHS-->Brightness: %f \t Color: %li \t frequency: %f", intensity, (long)hueVal, frequency);
        // do something with object
        //Get light from cache
        PHLight *light = [cache.lights objectForKey:@"3"];
    
        //Get Light state of this light
        PHLightState *state = light.lightState;
        
        //Change hue of this light state
        state.hue = [NSNumber numberWithInteger:(NSInteger) hueVal];
        state.saturation = @254;
    state.transitionTime = 0;
//    state.brightness = [NSNumber numberWithFloat:intensity];
        state.brightness = @254;
        PHBridgeSendAPI *BridgeSendAPI = [[PHBridgeSendAPI alloc] init];
        
        [BridgeSendAPI updateLightStateForId:@"3" withLightState:state completionHandler:nil];
//    }
    
    
//    [self updateLowBulbs:(maxSound / runningMaxSound) withFrequency:frequency];
//    PHBridgeResourcesCache cache = PHBridgeResourcesReader.readBridgeResourcesCache();
        highsFlag = false;
    }
}

- (void)updateMidBulbs:(float)intensity withFrequency:(float)frequency
{
    if(midsFlag)
    {
    NSInteger hueVal = mids[midsIndex];
    if(midsIndex + 1 > 3){
        midsIndex = 0;
    } else{
        midsIndex = midsIndex + 1;
    }
   
    PHBridgeResourcesCache *cache = [PHBridgeResourcesReader readBridgeResourcesCache];
    NSArray *myLights = [cache.lights allValues];
//    for (PHLight *light in myLights) {
        NSLog(@"MIDS-->Brightness: %f \t Color: %li \t frequency: %f", intensity, (long)hueVal, frequency);
        // do something with object
        //Get light from cache
        PHLight *light = [cache.lights objectForKey:@"2"];
    
        //Get Light state of this light
        PHLightState *state = light.lightState;
        
        //Change hue of this light state
        state.hue = [NSNumber numberWithInteger:(NSInteger) hueVal];
         state.saturation = @254;
    state.transitionTime = 0;
//        state.brightness = [NSNumber numberWithFloat:intensity];
         state.brightness = @254;
        PHBridgeSendAPI *BridgeSendAPI = [[PHBridgeSendAPI alloc] init];
        
        [BridgeSendAPI updateLightStateForId:@"2" withLightState:state completionHandler:nil];
//    }
        midsFlag = false;
    }
}

- (void)updateLowBulbs:(float)intensity withFrequency:(float)frequency
{
    if(lowsFlag)
    {
    NSInteger hueVal = lows[lowsIndex];
    if(lowsIndex + 1 > 3){
        lowsIndex = 0;
    } else{
        lowsIndex = lowsIndex + 1;
    }
    
    PHBridgeResourcesCache *cache = [PHBridgeResourcesReader readBridgeResourcesCache];
    NSArray *myLights = [cache.lights allValues];
//    for (PHLight *light in myLights) {
        NSLog(@"LOWS-->Brightness: %f \t Color: %li \t frequency: %f", intensity, (long)hueVal, frequency);
        // do something with object
        //Get light from cache
        PHLight *light = [cache.lights objectForKey:@"3"];
    
        //Get Light state of this light
        PHLightState *state = light.lightState;
        
        //Change hue of this light state
        state.hue = [NSNumber numberWithInteger:(NSInteger) hueVal];
         state.saturation = @254;
    state.transitionTime = 0;
//    state.brightness = [NSNumber numberWithFloat:intensity];
         state.brightness = @254;
        
        PHBridgeSendAPI *BridgeSendAPI = [[PHBridgeSendAPI alloc] init];
        
        [BridgeSendAPI updateLightStateForId:@"3" withLightState:state completionHandler:nil];
//    }
        lowsFlag = false;
    }
}



-(void)updateLowTimerFlag:(NSTimer *)timer
{
    lowsFlag = true;
}

-(void)updateHighTimerFlag:(NSTimer *)timer
{
    highsFlag = true;
}

-(void)updateMidTimerFlag:(NSTimer *)timer
{
    midsFlag = true;
}


#pragma mark - Customize the Audio Plot
- (void)viewDidLoad
{
    [super viewDidLoad];
    
    
    counter = 0;
    sendChange = false;
    
    NSTimer *lowTimer = [NSTimer scheduledTimerWithTimeInterval:0.25
                                     target:self
                                   selector:@selector(updateLowTimerFlag:)
                                   userInfo:nil
                                    repeats:YES];
    NSTimer *midTimer = [NSTimer scheduledTimerWithTimeInterval:0.25
                                                         target:self
                                                       selector:@selector(updateMidTimerFlag:)
                                                       userInfo:nil
                                                        repeats:YES];
    
    NSTimer *highTimer = [NSTimer scheduledTimerWithTimeInterval:0.25
                                                         target:self
                                                       selector:@selector(updateHighTimerFlag:)
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
    self.audioPlotFreq.backgroundColor = [UIColor colorWithRed: 0.114 green: 0.111 blue: 0.125 alpha: 1];
    self.audioPlotFreq.color           = [UIColor colorWithRed:1.0 green:1.0 blue:1.0 alpha:1.0];
    self.audioPlotFreq.shouldFill      = YES;
    self.audioPlotFreq.plotType        = EZPlotTypeBuffer;
    
    /*
     Start the microphone
     */
    self.microphone = [EZMicrophone microphoneWithDelegate:self
                                         startsImmediately:YES];
    NSLog(@"RAJWLRKJSF");
    
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
    
    int lhs = nOver2/3;
    int rhs = lhs*2;

    int index = 0;
   
    for(int i=0; i<nOver2; i++) {
        // Calculate the magnitude
        float mag = _A.realp[i]*_A.realp[i]+_A.imagp[i]*_A.imagp[i];
        
        //THIS IS SOME RANDOM SHIT-->
        if(mag > maxMag) {
            maxMag = mag;
            index = i;
        }
//        maxMag = mag > maxMag ? mag : maxMag;
        
        //END OF SOME RANDOM GOBBLYGOOP
    }
    frequency = (double) index * (44100.0 / nOver2 / 2.0);
    
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
            runningMaxSound = runningMaxSound > maxSound ? (runningMaxSound - .05)  : maxSound;
        else
            runningMaxSound = runningMaxSound > maxSound ? (runningMaxSound)  : maxSound;
        
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
        if(frequency < 1500){
            currentRangeBin = 3;
        } else if(frequency < 2500){
            currentRangeBin = 2;
        } else {
            currentRangeBin = 1;
        }
        
        
        /* Play with the sound-->Determine whether or not to CHANGE bulbs */
        if( (maxSound < 0 && runningMaxSound > 0) || (maxSound > 0 && runningMaxSound < 0) )
            maxSound = maxSound * -1;
//    NSLog(@"%f", (runningMaxSound / maxSound));
    
    long ratio = (runningMaxSound / maxSound);
        if( ratio < 3 ){
            //I think this is LOUD-->FORTISSIMO
//            NSLog(@"HARD HIT");
            if(sendChange){
                if(currentRangeBin == 1){
                    //SEND CHANGE TO BIN 1
//                    NSLog(@"HIGH");
                    //[self createFFTWithBufferSize:bufferSize withAudioData:buffer[0]];
                    [self updateHighBulbs:(maxSound / runningMaxSound) withFrequency:frequency];
                } else if(currentRangeBin == 2) {
                    //SEND CHANGE TO BIN 2
//                     NSLog(@"MID");
                    [self updateMidBulbs:(maxSound / runningMaxSound) withFrequency:frequency];
                } else if(currentRangeBin == 3){
                    //SNED CHANGE TO BIN 3
//                     NSLog(@"LOW");
                    [self updateLowBulbs:(maxSound / runningMaxSound) withFrequency:frequency];
                }
                
                sendChange = false;
            }
        } else {
            sendChange = true;
            //This is too quite to do anything about.
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
    [self.audioPlotFreq updateBuffer:amp
                      withBufferSize:nOver2];

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

@end