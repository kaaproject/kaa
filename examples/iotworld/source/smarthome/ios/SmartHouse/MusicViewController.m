/*
 * Copyright 2014-2015 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//
//  ViewController.m
//  SmartHouse
//
//  Created by Anton Bohomol on 4/3/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import "MusicViewController.h"

@interface MusicViewController () <DeviceEventDelegate, MusicEventDelegate, GeoFencingEventDelegate>

@property(strong,nonatomic) NSMutableArray *musicPlayers;
@property(strong,nonatomic) NSMutableArray *musicPlayerSection;
@property(strong,nonatomic) NSMutableDictionary *deviceInfoHolder;
@property(strong,nonatomic) NSMutableDictionary *playbackStatusHolder;
@property(strong,nonatomic) NSMutableDictionary *playListHolder;
@property(strong,nonatomic) NSMutableDictionary *geoFencingStatusHolder;
@property(strong,nonatomic) MusicDeviceController *deviceController;

- (void)waitAttaching:(AppDelegate *)app;

@end

@implementation MusicViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.barButton.target = self.revealViewController;
    self.barButton.action = @selector(revealToggle:);
    [self.view addGestureRecognizer:self.revealViewController.panGestureRecognizer];
    self.navigationController.navigationBar.hidden = true;
    
    self.loadingView.activityIndicatorViewStyle = UIActivityIndicatorViewStyleWhiteLarge;
    self.loadingView.tintColor = [UIColor greenColor];
    self.loadingView.color = [UIColor grayColor];
    
    self.deviceInfoHolder = [NSMutableDictionary dictionary];
    self.playbackStatusHolder = [NSMutableDictionary dictionary];
    self.playListHolder = [NSMutableDictionary dictionary];
    self.geoFencingStatusHolder = [NSMutableDictionary dictionary];
    self.musicPlayerSection = [NSMutableArray array];
    self.musicPlayers = [NSMutableArray arrayWithObjects: self.musicPlayerSection, nil];
    
    UINib *cellNib = [UINib nibWithNibName:@"MusicPlayerCell" bundle:nil];
    [self.collectionView registerNib:cellNib forCellWithReuseIdentifier:@"mpCell"];
    
    UICollectionViewFlowLayout *flowLayout = [[UICollectionViewFlowLayout alloc] init];
    [flowLayout setItemSize:CGSizeMake(280, 150)];
    [flowLayout setScrollDirection:UICollectionViewScrollDirectionVertical];
    [self.collectionView setCollectionViewLayout:flowLayout];
    
    AppDelegate *app = [[UIApplication sharedApplication] delegate];
    self.deviceController = app.musicDeviceController;
    self.deviceController.deviceDelegate = self;
    self.deviceController.musicDelegate = self;
    self.deviceController.geoFencingDelegate = self;
    [self.loadingView startAnimating];
    [self waitAttaching:app];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    self.deviceController.deviceDelegate = self;
    self.deviceController.musicDelegate = self;
    self.deviceController.geoFencingDelegate = self;
    if ([self.musicPlayerSection count] > 0) {
        for (NSData *key in self.musicPlayerSection) {
            [self.deviceController requestGeoFencingInfo:[Utils dataToEndpointId:key]];
            [self.deviceController requestDeviceInfo:[Utils dataToEndpointId:key]];
        }
    }
}

- (void)waitAttaching:(AppDelegate *)app {
    if ([app.kaaController isAttached]) {
        [self.deviceController findEventListeners];
    } else {
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            [self waitAttaching:app];
        });
    }
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
    if (![self.musicPlayerSection containsObject:key]) {
        [self.musicPlayerSection addObject:key];
    }
    [self.deviceInfoHolder setObject:name forKey:key];

    dispatch_async(dispatch_get_main_queue(), ^{
        [self.loadingView stopAnimating];
        self.loadingView.hidden = true;
        [self.collectionView reloadData];
    });

    [self.deviceController subscribeForUpdatesRequest:endpoinId];
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
    [self.playbackStatusHolder setObject:status forKey:key];
    
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

-(NSInteger)numberOfSectionsInCollectionView:(UICollectionView *)collectionView {
    return [self.musicPlayers count];
}

-(NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return [self.musicPlayers[section] count];
}

-(UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    
    NSMutableArray *data = self.musicPlayers[indexPath.section];
    NSString *key = data[indexPath.row];
    MusicPlayerCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"mpCell" forIndexPath:indexPath];
    [cell.layer setCornerRadius:5];
    
    cell.cover.hidden = YES;
    cell.deviceName.text = [self.deviceInfoHolder objectForKey:key];
    PlaybackStatus *playback = [self.playbackStatusHolder objectForKey:key];
    cell.progressBar.hidden = playback ? NO : YES;
    cell.noDataLabel.text = playback ? @"" : @"No data available";
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
    NSMutableArray *data = self.musicPlayers[indexPath.section];
    NSData *key = data[indexPath.row];
    kaa_endpoint_id_p endpointId = [Utils dataToEndpointId:key];
    NSString *deviceName = [self.deviceInfoHolder objectForKey:key];
    NSMutableArray *albums = [self.playListHolder objectForKey:key];
    AlbumsViewController *viewController = [self.storyboard instantiateViewControllerWithIdentifier:@"AlbumsViewController"];
    [viewController setDeviceName:deviceName withAlbums:albums andEndpointId:endpointId];
    [self presentViewController:viewController animated:YES completion:nil];
}

@end
