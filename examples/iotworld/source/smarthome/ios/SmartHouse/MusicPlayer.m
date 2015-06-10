//
//  MusicPlayer.m
//  SmartHouse
//
//  Created by Anton Bohomol on 4/16/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import "MusicPlayer.h"


@interface MusicPlayer () <MusicEventDelegate, GeoFencingEventDelegate>

@property(assign,nonatomic) kaa_endpoint_id_p endpointId;
@property(strong,nonatomic) SongInfo* currentSong;
@property(strong,nonatomic) AlbumInfo *albumInfo;
@property(strong,nonatomic) MusicDeviceController *deviceController;
@property(nonatomic) BOOL isPlaying;
@property(nonatomic) kaa_geo_fencing_event_class_family_operation_mode_t currentMode;
@property(nonatomic) BOOL isVolumeManuallySet;
@property(nonatomic) BOOL isTimeManuallySet;
@property(nonatomic) BOOL isPlayingManuallySet;

- (void)play:(SongInfo*) song;
- (void)pause;

@end

@implementation MusicPlayer

- (void)setAlbumInfo:(AlbumInfo *)albumInfo forEndpointId:(kaa_endpoint_id_p)endpointId {
    self.albumInfo = albumInfo;
    self.endpointId = endpointId;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    [self.table setSeparatorStyle:UITableViewCellSeparatorStyleNone];
    self.singerLabel.text = _albumInfo.artist;
    self.navBar.title = _albumInfo.title;
    
    AppDelegate *app = [[UIApplication sharedApplication] delegate];
    self.deviceController = app.musicDeviceController;
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    self.deviceController.musicDelegate = self;
    self.deviceController.geoFencingDelegate = self;
    [self.deviceController subscribeForUpdatesRequest:self.endpointId];
}

- (IBAction)volumeChanged:(id)sender {
    UISlider *slider = (UISlider *)sender;
    self.isVolumeManuallySet = YES;
    [self.deviceController requestVolumeChange:slider.value forEndpoint:self.endpointId];
}

- (IBAction)timeChanged:(id)sender {
    UISlider *slider = (UISlider *)sender;
    self.isTimeManuallySet = YES;
    [self.deviceController requestTimeChange:slider.value forEndpoint:self.endpointId];
}

- (IBAction)playStopButton {
    if (self.isPlaying) {
        [self pause];
    } else if (_currentSong) {
        [self play:_currentSong];
    }
}

- (IBAction)back {
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (void)play:(SongInfo *)song {
    self.isPlayingManuallySet = YES;
    [self.deviceController play:song.url onEndpoint:self.endpointId];
}

- (void)pause {
    self.isPlayingManuallySet = YES;
    [self.deviceController pause:self.endpointId];
}

- (IBAction)invokeGeoFencingPopup:(id)sender {
    UIActionSheet *actionSheet = [[UIActionSheet alloc] initWithTitle:@"Geo Fencing"
                                                             delegate:self
                                                    cancelButtonTitle:nil
                                               destructiveButtonTitle:nil
                                                    otherButtonTitles:nil];
    NSArray *array = [Utils geoFencingModesWithCurrent:self.currentMode];
    for (NSString *title in array) {
        [actionSheet addButtonWithTitle:title];
    }
    [actionSheet addButtonWithTitle:@"Cancel"];
    [actionSheet showInView:self.view];
}

- (void)onPlaybackReceived:(PlaybackStatus*)status forEndpoint:(kaa_endpoint_id_p)endpointId {
    if ([self.albumInfo.albumId isEqualToString:status.song.albumId]) {
        dispatch_async(dispatch_get_main_queue(), ^{
            if (!self.isPlayingManuallySet) {
                self.isPlaying = status.status == ENUM_PLAYBACK_STATUS_PLAYING ? YES : NO;
                self.playControlButton.imageView.image = [UIImage imageNamed: (self.isPlaying ? @"pause" : @"play")];
            }
            self.isPlayingManuallySet = NO;
            if (!status.ignoreVolumeUpdate && !self.isVolumeManuallySet) {
                self.volumeSlider.maximumValue = status.maxVolume;
                self.volumeSlider.value = status.volume;
            }
            self.isVolumeManuallySet = NO;
            if (status.song) {
                if (!status.ignoreTimeUpdate && !self.isTimeManuallySet) {
                    self.progressLabel.text = [status formattedTimeProgress];
                    self.timeSlider.maximumValue = status.song.duration;
                    self.timeSlider.value = status.time;
                }
                self.isTimeManuallySet = NO;
                NSString *prevUrl = self.currentSong.url;
                self.currentSong = status.song;
                if (![prevUrl isEqualToString:status.song.url]) {
                    [self.table reloadData];
                }
                self.songLabel.text = status.song.title;
            }
        });
    }
}

- (void)onStatusReceived:(kaa_geo_fencing_event_class_family_operation_mode_t)mode forEndpoint:(kaa_endpoint_id_p)endpointId {
    self.currentMode = mode;
}

#pragma mark - TableView callbacks

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [_albumInfo.songs count];
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    SongInfo *song = _albumInfo.songs[indexPath.row];
    if ([self.currentSong.url isEqualToString:song.url] && self.isPlaying) {
        [self pause];
    } else {
        [self play:song];
    }
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    SongViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"SongItem"];
    
    if (cell == nil) {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"SongViewCell" owner:self options:nil];
        cell = nib[0];
    }

    cell.selectionStyle = UITableViewCellSelectionStyleDefault;
    UIView *bgColorView = [[UIView alloc] init];
    bgColorView.backgroundColor = [UIColor colorWithRed:246.0/255.0 green:113.0/255.0 blue:32.0/255.0 alpha:0.2];
    [cell setSelectedBackgroundView:bgColorView];

    SongInfo *song = _albumInfo.songs[indexPath.row];
    cell.songName.text = song.title;
    cell.duration.text = [Utils millisToMinutesTime:song.duration];
    return cell;
}

- (void)tableView:(UITableView *)tableView willDisplayCell:(UITableViewCell *)cell forRowAtIndexPath:(NSIndexPath *)indexPath {
    SongInfo *song = _albumInfo.songs[indexPath.row];
    if (self.currentSong) {
        BOOL isSelected = [self.currentSong.url isEqualToString:song.url];
        [cell setSelected:isSelected animated:NO];
    }
}

- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
    //we rely on fact that we have 3 buttons and according to that 3 modes which values are
    //exactly that same as button indexes
    if (buttonIndex < 3) {
        [self.deviceController changeGeoFencingMode:buttonIndex forEndpoint:self.endpointId];
        //update current mode manually so we wouldn't wait for response
        self.currentMode = buttonIndex;
    }
}

- (void)willPresentActionSheet:(UIActionSheet *)actionSheet {
    for (UIView *subview in actionSheet.subviews) {
        if ([subview isKindOfClass:[UIButton class]]) {
            UIButton *button = (UIButton *)subview;
            [button setTitleColor:[UIColor colorWithRed:246.0/255.0 green:113.0/255.0 blue:32.0/255.0 alpha:1.0] forState:UIControlStateNormal];
        }
    }
}

@end
