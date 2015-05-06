//
//  AlbumsViewController.m
//  SmartHouse
//
//  Created by Anton Bohomol on 4/19/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import "AlbumsViewController.h"

@interface AlbumsViewController () <MusicEventDelegate, GeoFencingEventDelegate, DeviceEventDelegate>

@property(assign,nonatomic) kaa_endpoint_id_p endpointId;
@property(strong,nonatomic) NSString *deviceName;
@property(strong,nonatomic) NSMutableArray *albums;
@property(strong,nonatomic) NSMutableArray *albumsSection;
@property(strong,nonatomic) MusicDeviceController *deviceController;
//values to represent current playing item
@property(nonatomic) NSInteger currentAlbumIndex;
@property(strong,nonatomic) PlaybackStatus *currentPlayback;
@property(nonatomic) kaa_geo_fencing_event_class_family_operation_mode_t currentMode;
@property(strong,nonatomic) UIActionSheet *mainSheet;

- (void)showGeoFencingActionSheet;
- (void)changeDeviceName;

@end

@implementation AlbumsViewController

- (void)setDeviceName:(NSString *)name withAlbums:(NSMutableArray*)albums andEndpointId:(kaa_endpoint_id_p)endpointId {
    self.deviceName = name;
    self.albumsSection = albums;
    self.endpointId = endpointId;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.navBar.title = self.deviceName;
    self.currentAlbumIndex = -1;
    self.currentPlayback = nil;
    if (!self.albumsSection) {
        self.albumsSection = [[NSMutableArray alloc] init];
    }
    self.albums = [[NSMutableArray alloc] initWithObjects:self.albumsSection, nil];
    
    UINib *cellNib = [UINib nibWithNibName:@"AlbumCell" bundle:nil];
    [self.collectionView registerNib:cellNib forCellWithReuseIdentifier:@"albumCell"];
    
    UICollectionViewFlowLayout *flowLayout = [[UICollectionViewFlowLayout alloc] init];
    [flowLayout setItemSize:CGSizeMake(280, 150)];
    [flowLayout setScrollDirection:UICollectionViewScrollDirectionVertical];
    [self.collectionView setCollectionViewLayout:flowLayout];

    AppDelegate *app = [[UIApplication sharedApplication] delegate];
    self.deviceController = app.musicDeviceController;
    self.deviceController.deviceDelegate = self;
    self.deviceController.musicDelegate = self;
    self.deviceController.geoFencingDelegate = self;
    
    [self.deviceController subscribeForUpdatesRequest:self.endpointId];
    [self.deviceController requestGeoFencingInfo:self.endpointId];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    self.deviceController.deviceDelegate = self;
    self.deviceController.musicDelegate = self;
    self.deviceController.geoFencingDelegate = self;
}

- (IBAction)volumeChanged:(id)sender {
    UISlider *slider = (UISlider *)sender;
    [self.deviceController requestVolumeChange:slider.value forEndpoint:self.endpointId];
}

- (IBAction)back {
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (void)onPlayListReceived:(NSArray*)albums forEndpoint:(kaa_endpoint_id_p)endpointId {
    [self.albumsSection addObjectsFromArray:albums];
    dispatch_async( dispatch_get_main_queue(), ^{
        [self.collectionView reloadData];
    });
}

- (void)onPlaybackReceived:(PlaybackStatus*)status forEndpoint:(kaa_endpoint_id_p)endpointId {
    if (!status.ignoreVolumeUpdate) {
        self.volumeSlider.maximumValue = status.maxVolume;
        self.volumeSlider.value = status.volume;
    }
    if (!status.song) {
        return;
    }
    NSUInteger albumsLength = [self.albumsSection count];
    for (int i = 0; i < albumsLength; i++) {
        AlbumInfo *album = self.albumsSection[i];
        if ([album.albumId isEqualToString:status.song.albumId]) {
            self.currentAlbumIndex = i;
            self.currentPlayback = status;
            dispatch_async( dispatch_get_main_queue(), ^{
                [self.collectionView reloadData];
            });
            break;
        }
    }
}

- (void)onStatusReceived:(kaa_geo_fencing_event_class_family_operation_mode_t)mode forEndpoint:(kaa_endpoint_id_p)endpointId {
    self.currentMode = mode;
}

- (void)foundDevice:(NSString *)name forEndpoint:(kaa_endpoint_id_p)endpoinId {
    dispatch_async( dispatch_get_main_queue(), ^{
        self.deviceName = name;
        self.navBar.title = name;
    });
}

#pragma mark - Collection view callbacks

-(NSInteger)numberOfSectionsInCollectionView:(UICollectionView *)collectionView {
    return [self.albums count];
}

-(NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return [self.albums[section] count];
}

-(UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    NSMutableArray *data = self.albums[indexPath.section];
    AlbumInfo *album = data[indexPath.row];
    
    AlbumCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"albumCell" forIndexPath:indexPath];
    [cell.layer setCornerRadius:5];

    cell.album.text = album.title;
    cell.artist.text = album.artist;
    NSInteger trackCount = [album.songs count];
    cell.tracks.text = [NSString stringWithFormat:@"%lu %@", (long)trackCount, (trackCount == 1 ? @"track" : @"tracks")];
    cell.cover.image = album.cover;
    
    if (indexPath.row == _currentAlbumIndex && _currentPlayback && _currentPlayback.song) {

        cell.song.text = _currentPlayback.song.title;
        cell.time.text = [_currentPlayback formattedTimeProgress];
        cell.time.hidden = NO;
        if (!_currentPlayback.ignoreTimeUpdate) {
            cell.progressBar.progress = (float)_currentPlayback.time / (float)_currentPlayback.song.duration;
            cell.progressBar.hidden = NO;
        }
    } else {
        cell.song.text = @"No track selected";
        cell.time.hidden = YES;
        cell.progressBar.hidden = YES;
    }
    
    return cell;
    
}

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    NSMutableArray *data = self.albums[indexPath.section];
    AlbumInfo *albumInfo = data[indexPath.row];
    MusicPlayer *music = [self.storyboard instantiateViewControllerWithIdentifier:@"MusicPlayer"];
    [music setAlbumInfo:albumInfo forEndpointId:_endpointId];
    [self presentViewController:music animated:YES completion:nil];
}


- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex {
    if (buttonIndex != alertView.cancelButtonIndex) {
        NSString *name = [[alertView textFieldAtIndex:0] text];
        AppDelegate *app = [[UIApplication sharedApplication] delegate];
        [app.kaaController renameEndpoint:self.endpointId to:name];
    }
}

- (BOOL)alertViewShouldEnableFirstOtherButton:(UIAlertView *)alertView {
    NSString *inputText = [[alertView textFieldAtIndex:0] text];
    return [inputText length] > 0 ? YES : NO;
}

- (void)changeDeviceName {
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"New device name:"
                                                    message:nil
                                                   delegate:self
                                          cancelButtonTitle:@"Cancel"
                                          otherButtonTitles:@"Rename", nil];
    
    [alert setAlertViewStyle:UIAlertViewStylePlainTextInput];
    [alert textFieldAtIndex:0].text = self.deviceName;
    [alert show];
}

- (IBAction)settings:(id)sender {
    self.mainSheet = [[UIActionSheet alloc] initWithTitle:nil
                                                 delegate:self
                                        cancelButtonTitle:nil
                                   destructiveButtonTitle:nil
                                        otherButtonTitles:@"Rename device", @"Geo fencing", nil];
    [self.mainSheet addButtonWithTitle:@"Cancel"];
    [self.mainSheet showInView:self.view];
}

- (void)showGeoFencingActionSheet {
    UIActionSheet *geoFencingSheet = [[UIActionSheet alloc] initWithTitle:nil
                                                                 delegate:self
                                                        cancelButtonTitle:nil
                                                   destructiveButtonTitle:nil
                                                        otherButtonTitles:nil];
    NSArray *modes = [Utils geoFencingModesWithCurrent:self.currentMode];
    for (NSString *mode in modes) {
        [geoFencingSheet addButtonWithTitle:mode];
    }
    [geoFencingSheet addButtonWithTitle:@"Cancel"];
    [geoFencingSheet showInView:self.view];
}

- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
    if (actionSheet == self.mainSheet) {
        switch (buttonIndex) {
            case 0:
                [self changeDeviceName];
                break;
            case 1:
                [self showGeoFencingActionSheet];
                break;
        }
    } else {
        //we rely on fact that we have 3 buttons and according to that 3 modes which values are
        //exactly that same as button indexes
        if (buttonIndex < 3) {
            [self.deviceController changeGeoFencingMode:buttonIndex forEndpoint:self.endpointId];
            //update current mode manually so we wouldn't wait for response
            self.currentMode = buttonIndex;
        }
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