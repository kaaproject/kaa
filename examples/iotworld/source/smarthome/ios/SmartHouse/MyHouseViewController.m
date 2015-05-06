//
//  MyHouseViewController.m
//  SmartHouse
//
//  Created by Anton Bohomol on 4/9/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import "MyHouseViewController.h"
#import "SWRevealViewController.h"

@interface MyHouseViewController () <DeviceEventDelegate, PhotoFrameDelegate, MusicEventDelegate, GeoFencingEventDelegate>

@property(strong,nonatomic) NSMutableArray *devices;
@property(strong,nonatomic) NSMutableArray *devicesSection;
@property(strong,nonatomic) NSMutableDictionary *deviceInfoHolder;
@property(strong,nonatomic) NSMutableDictionary *deviceStatusHolder;
@property(strong,nonatomic) NSMutableDictionary *playListHolder;
@property(strong,nonatomic) NSMutableDictionary *geoFencingStatusHolder;
@property(strong,nonatomic) MusicDeviceController *musicController;
@property(strong,nonatomic) PhotoDeviceController *photoController;
@property(strong,nonatomic) KaaController *kaaController;

- (void)appendDelegates;
- (UICollectionViewCell*)photoFrameCell:(UICollectionView *)view indexPath:(NSIndexPath *)indexPath key:(NSString*)key;
- (UICollectionViewCell*)musicPlayerCell:(UICollectionView *)view indexPath:(NSIndexPath *)indexPath key:(NSString*)key;
- (void)waitAttaching;

@end

@implementation MyHouseViewController

- (void)viewDidLoad {
    [super viewDidLoad];

    self.barButton.target = self.revealViewController;
    self.barButton.action = @selector(revealToggle:);
    [self.view addGestureRecognizer:self.revealViewController.panGestureRecognizer];
    
    self.loadingView.activityIndicatorViewStyle = UIActivityIndicatorViewStyleWhiteLarge;
    self.loadingView.tintColor = [UIColor greenColor];
    self.loadingView.color = [UIColor grayColor];
    
    self.deviceInfoHolder = [NSMutableDictionary dictionary];
    self.deviceStatusHolder = [NSMutableDictionary dictionary];
    self.playListHolder = [NSMutableDictionary dictionary];
    self.geoFencingStatusHolder = [NSMutableDictionary dictionary];
    self.devicesSection = [NSMutableArray array];
    self.devices = [NSMutableArray arrayWithObjects:self.devicesSection, nil];
    
    UINib *musicPlayerNib = [UINib nibWithNibName:@"MusicPlayerCell" bundle:nil];
    UINib *photoFrameNib = [UINib nibWithNibName:@"PhotoFrameCell" bundle:nil];
    [self.collectionView registerNib:photoFrameNib forCellWithReuseIdentifier:@"pfCell"];
    [self.collectionView registerNib:musicPlayerNib forCellWithReuseIdentifier:@"mpCell"];
    
    UICollectionViewFlowLayout *flowLayout = [[UICollectionViewFlowLayout alloc] init];
    [flowLayout setItemSize:CGSizeMake(280, 150)];
    [flowLayout setMinimumLineSpacing:20];
    [flowLayout setScrollDirection:UICollectionViewScrollDirectionVertical];
    [self.collectionView setCollectionViewLayout:flowLayout];
    
    [self.loadingView startAnimating];
    
    AppDelegate *app = [[UIApplication sharedApplication] delegate];
    self.kaaController = app.kaaController;
    self.photoController = app.photoDeviceController;
    self.musicController = app.musicDeviceController;
    
    [self appendDelegates];
    [self waitAttaching];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self appendDelegates];
    if ([self.devicesSection count] > 0) {
        for (NSData *key in self.devicesSection) {
            [self.kaaController requestGeoFencingInfo:[Utils dataToEndpointId:key] delegate:self];
            [self.kaaController requestDeviceInfo:[Utils dataToEndpointId:key] delegate:self];
        }
    }
}

- (void)waitAttaching {
    if ([self.kaaController isAttached]) {
        [self.photoController findEventListeners];
        [self.musicController findEventListeners];
    } else {
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.5 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            [self waitAttaching];
        });
    }
}

- (void)appendDelegates {
    self.musicController.deviceDelegate = self;
    self.musicController.musicDelegate = self;
    self.musicController.geoFencingDelegate = self;
    self.photoController.deviceDelegate = self;
    self.photoController.photoFrameDelegate = self;
    self.photoController.geoFencingDelegate = self;
}

- (void)noDevicesFound {
    //TODO let user know that no devices been found
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.loadingView stopAnimating];
        self.loadingView.hidden = true;
    });
    
}

- (void)foundDevice:(NSString*)name forEndpoint:(kaa_endpoint_id_p)endpoinId {
    NSData *key = [Utils endpointIdToData:endpoinId];
    if (![self.devicesSection containsObject:key]) {
        [self.devicesSection addObject:key];
    }
    [self.deviceInfoHolder setObject:name forKey:key];

    dispatch_async(dispatch_get_main_queue(), ^{
        [self.loadingView stopAnimating];
        self.loadingView.hidden = true;
        [self.collectionView reloadData];
    });
    
    [self.kaaController subscribeForUpdates:endpoinId];
}

- (void)onPlayListReceived:(NSArray*)albums forEndpoint:(kaa_endpoint_id_p)endpointId {
    NSData *key = [Utils endpointIdToData:endpointId];
    [self.playListHolder setObject:albums forKey:key];
    
    dispatch_async( dispatch_get_main_queue(), ^{
        [self.collectionView reloadData];
    });
}

- (void)onPlaybackReceived:(PlaybackStatus*)status forEndpoint:(kaa_endpoint_id_p)endpointId {
    NSData *key = [Utils endpointIdToData:endpointId];
    [self.deviceStatusHolder setObject:status forKey:key];
    
    dispatch_async( dispatch_get_main_queue(), ^{
        [self.collectionView reloadData];
    });
}

- (void)onAlbumsReceived:(NSArray*)albums fromEndpoint:(kaa_endpoint_id_p)endpointId {
    NSData *key = [Utils endpointIdToData:endpointId];
    [self.playListHolder setObject:albums forKey:key];
    
    dispatch_async( dispatch_get_main_queue(), ^{
        [self.collectionView reloadData];
    });
}

- (void)onAlbumStatusUpdate:(PhotoFrameStatus*)status fromEndpoint:(kaa_endpoint_id_p)endpointId {
    NSData *key = [Utils endpointIdToData:endpointId];
    [self.deviceStatusHolder setObject:status forKey:key];
    
    dispatch_async( dispatch_get_main_queue(), ^{
        [self.collectionView reloadData];
    });
}

- (void)onStatusReceived:(kaa_geo_fencing_event_class_family_operation_mode_t)mode forEndpoint:(kaa_endpoint_id_p)endpointId {
    NSData *key = [Utils endpointIdToData:endpointId];
    [self.geoFencingStatusHolder setObject:[Utils geoFencingModeToString:mode] forKey:key];
    
    dispatch_async( dispatch_get_main_queue(), ^{
        [self.collectionView reloadData];
    });
}

#pragma mark - Collection view callbacks

- (NSInteger)numberOfSectionsInCollectionView:(UICollectionView *)collectionView {
    return [self.devices count];
}

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return [self.devices[section] count];
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    
    NSMutableArray *data = self.devices[indexPath.section];
    NSString *key = data[indexPath.row];
    DeviceType type = [[self.kaaController.deviceTypesRegistry objectForKey:key] intValue];
    
    switch (type) {
        case MusicPlayerType:
            return [self musicPlayerCell:collectionView indexPath:indexPath key:key];
        case PhotoFrameType:
            return [self photoFrameCell:collectionView indexPath:indexPath key:key];
        default:
            return nil;
    }
}

- (UICollectionViewCell*)photoFrameCell:(UICollectionView *)view indexPath:(NSIndexPath *)indexPath key:(NSString*)key {
        PhotoFrameCell *cell = [view dequeueReusableCellWithReuseIdentifier:@"pfCell" forIndexPath:indexPath];
        [cell.layer setCornerRadius:5];
    
        cell.deviceName.text = [self.deviceInfoHolder objectForKey:key];
        NSString* geoFencingStatus = [self.geoFencingStatusHolder objectForKey:key];
        cell.geoFencingState.text = geoFencingStatus ? geoFencingStatus : @"";
        PhotoFrameStatus *status = [self.deviceStatusHolder objectForKey:key];
        cell.thumbnail.image = status.thumbnail;
        cell.noDataLabel.text = cell.thumbnail.image ? @"" : @"No data available";
    
        cell.thumbnail.hidden = cell.thumbnail.image ? NO : YES;
        cell.mode.text = status.status == ENUM_SLIDE_SHOW_STATUS_PLAYING ? @"Slideshow mode" : @"";
        if (!status || !status.albumId) {
            return cell;
        }
    
        NSMutableArray *albums = [self.playListHolder objectForKey:key];
        if (!albums) {
            return cell;
        }
        for (PhotoAlbumInfo *album in albums) {
            if ([album.albumId isEqualToString:status.albumId]) {
                cell.album.text = album.title;
                cell.photoPosition.text = [NSString stringWithFormat:@"Photo %d of %d", status.photoNumber, album.size];
                break;
            }
        }
      return cell;
}

- (UICollectionViewCell*)musicPlayerCell:(UICollectionView *)view indexPath:(NSIndexPath *)indexPath key:(NSString*)key {
    MusicPlayerCell *cell = [view dequeueReusableCellWithReuseIdentifier:@"mpCell" forIndexPath:indexPath];
    [cell.layer setCornerRadius:5];
    
    cell.deviceName.text = [self.deviceInfoHolder objectForKey:key];
    PlaybackStatus *playback = [self.deviceStatusHolder objectForKey:key];
    cell.progressBar.hidden = playback ? NO : YES;
    cell.noDataLabel.text = playback ? @"" : @"No data available";
    cell.cover.hidden = YES;
    NSString* geoFencingStatus = [self.geoFencingStatusHolder objectForKey:key];
    cell.state.text = geoFencingStatus ? geoFencingStatus : @"";
    if (!playback || !playback.song) {
        return cell;
    }
    
    if (!playback.ignoreTimeUpdate) {
        cell.progressBar.progress = (float)playback.time / (float)playback.song.duration;
        cell.time.text = [playback formattedTimeProgress];
    }
    cell.song.text = playback.song.title;
    NSMutableArray *albums = [self.playListHolder objectForKey:key];
    if (!albums) {
        return cell;
    }
    for (AlbumInfo *album in albums) {
        if ([album.albumId isEqualToString:playback.song.albumId]) {
            cell.artist.text = album.artist;
            cell.album.text = album.title;
            cell.cover.image = album.cover;
            cell.cover.hidden = NO;
            break;
        }
    }
    
    return cell;
}

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    NSMutableArray *data = self.devices[indexPath.section];
    NSData *key = data[indexPath.row];
    kaa_endpoint_id_p endpointId = [Utils dataToEndpointId:key];
    NSString *deviceName = [self.deviceInfoHolder objectForKey:key];
    NSMutableArray *albums = [self.playListHolder objectForKey:key];
    DeviceType type = [[self.kaaController.deviceTypesRegistry objectForKey:key] intValue];
    switch (type) {
        case MusicPlayerType: {
            AlbumsViewController *viewController = [self.storyboard instantiateViewControllerWithIdentifier:@"AlbumsViewController"];
            [viewController setDeviceName:deviceName withAlbums:albums andEndpointId:endpointId];
            [self presentViewController:viewController animated:YES completion:nil];
        }
            break;
        case PhotoFrameType: {
            PhotoAlbumViewController *viewController = [self.storyboard instantiateViewControllerWithIdentifier:@"PhotoAlbumViewController"];
            [viewController setDeviceName:deviceName withAlbums:albums andEndpointId:endpointId];
            [self presentViewController:viewController animated:YES completion:nil];
        }
            break;
    }
}

@end
