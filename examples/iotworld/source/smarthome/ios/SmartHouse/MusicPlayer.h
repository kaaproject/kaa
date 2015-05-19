//
//  MusicPlayer.h
//  SmartHouse
//
//  Created by Anton Bohomol on 4/16/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MusicViewController.h"
#import "MusicDeviceController.h"
#import "SongViewCell.h"
#import "Utils.h"
#import "AlbumInfo.h"
#import "SongInfo.h"
#import "PlaybackStatus.h"

@interface MusicPlayer : UIViewController <UITableViewDelegate, UITableViewDataSource, UIActionSheetDelegate>

@property(weak,nonatomic) IBOutlet UIBarButtonItem *backButton;
@property(weak,nonatomic) IBOutlet UISlider *volumeSlider;
@property(weak,nonatomic) IBOutlet UILabel *songLabel;
@property(weak,nonatomic) IBOutlet UILabel *singerLabel;
@property(weak,nonatomic) IBOutlet UIButton *playControlButton;
@property(weak,nonatomic) IBOutlet UISlider *timeSlider;
@property(weak,nonatomic) IBOutlet UILabel *progressLabel;
@property(weak,nonatomic) IBOutlet UITableView *table;
@property(weak,nonatomic) IBOutlet UINavigationItem *navBar;

- (void)setAlbumInfo:(AlbumInfo *)albumInfo forEndpointId:(kaa_endpoint_id_p)endpointId;
- (IBAction)playStopButton;
- (IBAction)volumeChanged:(id)sender;
- (IBAction)timeChanged:(id)sender;
- (IBAction)back;

@end