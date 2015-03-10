//
//  MusicController.h
//  Prism Lights
//
//  Created by Alma Knudson on 2/19/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

#import <UIKit/UIKit.h>

/**
 EZAudio
 */
#import "EZAudio.h"

/**
 Accelerate
 */
#import <Accelerate/Accelerate.h>

#import "Prism Lights-Bridging-Header.h"
#import "Prism_Lights-Swift.h"



/**
 The FFTViewController demonstrates how to use the Accelerate framework to calculate the real-time FFT of audio data provided by an EZAudioMicrophone.
 */
@interface MusicController : UIViewController <EZMicrophoneDelegate, BulbRangeSelectionDelegate>




#pragma mark - Components
/**
 EZAudioPlot for frequency plot
 */
@property (nonatomic,weak) IBOutlet EZAudioPlot *audioPlotFreq;

/**
 EZAudioPlot for time plot
 */
@property (nonatomic,weak) IBOutlet EZAudioPlotGL *audioPlotTime;

/**
 Microphone
 */
@property (nonatomic,strong) EZMicrophone *microphone;

@end



