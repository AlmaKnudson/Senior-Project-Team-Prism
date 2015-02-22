//
//  MusicController.m
//  Prism Lights
//
//  Created by Alma Knudson on 2/19/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "MusicController.h"

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
float runningMaxSound = 0;

bool sendChange;

#pragma mark - Customize the Audio Plot
- (void)viewDidLoad
{
    [super viewDidLoad];
    
    
    counter = 0;
    sendChange = false;
    
    
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
    if(counter > 4){
        counter = 0;
        
    
    // For an FFT, numSamples must be a power of 2, i.e. is always even
    int nOver2 = bufferSize/2;
    
    // Pack samples:
    // C(re) -> A[n], C(im) -> A[n+1]
    vDSP_ctoz((COMPLEX*)data, 2, &_A, 1, nOver2);
    
    // Perform a forward FFT using fftSetup and A
    // Results are returned in A
    vDSP_fft_zrip(_FFTSetup, &_A, 1, _log2n, FFT_FORWARD);
    
    // Convert COMPLEX_SPLIT A result to magnitudes
    float amp[nOver2];
    float maxMag = 0;
    
    for(int i=0; i<nOver2; i++) {
        // Calculate the magnitude
        float mag = _A.realp[i]*_A.realp[i]+_A.imagp[i]*_A.imagp[i];
        maxMag = mag > maxMag ? mag : maxMag;
    }
    for(int i=0; i<nOver2; i++) {
        // Calculate the magnitude
        float mag = _A.realp[i]*_A.realp[i]+_A.imagp[i]*_A.imagp[i];
        // Bind the value to be less than 1.0 to fit in the graph
        amp[i] = [EZAudio MAP:mag leftMin:0.0 leftMax:maxMag rightMin:0.0 rightMax:1.0];
    }
    float maxSound = data[0];
    for(int i = 0; i < bufferSize; i ++){
        maxSound = maxSound > data[i] ? maxSound  : data[i];
        runningMaxSound = runningMaxSound > maxSound ? (runningMaxSound - .00005)  : maxSound;
    }
        /* Play with the frequency-->Determine high/mid/low*/
        if(maxMag > max){
            max = maxMag;
        } else{
            max = max - 200;
        }
        if(maxMag < min){
            min = maxMag;
        } else {
            min = min + 20;
        }
        // NSLog(@"%f \t %f", max, min);
        /* Plotting the time domain plot takes around 50-60% CPU... HOLY MOSES */
        
        if(maxMag > min && maxMag < (max / 5)){
            currentRangeBin = 1;
            //            NSLog(@"LOWS");
        } else if(maxMag > (max / 5) && maxMag < (max - (max / 5))){
            currentRangeBin = 2;
            //            NSLog(@"MID");
        } else if(maxMag > (max - (max / 5))){
            currentRangeBin = 3;
            //            NSLog(@"HIGHS");
        }
        
        
        /* Play with the sound-->Determine whether or not to CHANGE bulbs */
        if(maxSound < 0)
            maxSound = maxSound * -1;
        if( (runningMaxSound / maxSound) < 2 ){
            //I think this is LOUD-->FORTISSIMO
//            NSLog(@"HARD HIT");
            if(sendChange){
                if(currentRangeBin == 1){
                    //SEND CHANGE TO BIN 1
                    NSLog(@"HIGH");
                } else if(currentRangeBin == 2) {
                    //SEND CHANGE TO BIN 2
                     NSLog(@"MID");
                } else if(currentRangeBin == 3){
                    //SNED CHANGE TO BIN 3
                     NSLog(@"LOW");
                }
                
                sendChange = false;
            }
        } else if( (runningMaxSound / maxSound) < 4 ){
            //This is medium loudness. meto forte
//            NSLog(@"MEDIUM HIT");
            sendChange = true;
        } else if( (runningMaxSound / maxSound) < 6 ){
            //This is quiter.
//            NSLog(@"SOFT HIT");
            sendChange = true;
        } else {
            //This is too quite to do anything about.
        }
       // NSLog(@"%f", maxSound);
        
        /* Plotting frequency domain + FFT --> 3% CPU and minimal memory footprint */
    // Update the frequency domain plot
    [self.audioPlotFreq updateBuffer:amp
                      withBufferSize:nOver2];

        
        // Update time domain plot
        [self.audioPlotTime updateBuffer:data
                          withBufferSize:bufferSize];
        
        
        
    } else {
        counter = counter + 1;
    }
    
    
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