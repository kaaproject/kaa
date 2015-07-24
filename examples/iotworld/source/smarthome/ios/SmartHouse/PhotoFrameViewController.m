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
//  PhotoFrameViewController.m
//  SmartHouse
//
//  Created by Anton Bohomol on 4/6/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import "PhotoFrameViewController.h"
#import "SWRevealViewController.h"

@interface PhotoFrameViewController () <DeviceEventDelegate, PhotoFrameDelegate, GeoFencingEventDelegate>

@property(strong,nonatomic) NSMutableArray *photoFrames;
@property(strong,nonatomic) NSMutableArray *photoFrameSection;
@property(strong,nonatomic) NSMutableDictionary *deviceInfoHolder;
@property(strong,nonatomic) NSMutableDictionary *photoFrameStatusHolder;
@property(strong,nonatomic) NSMutableDictionary *albumsListHolder;
@property(strong,nonatomic) NSMutableDictionary *geoFencingStatusHolder;
@property(strong,nonatomic) PhotoDeviceController *deviceController;

- (void)waitAttaching:(AppDelegate *)app;

@end

@implementation PhotoFrameViewController

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
    self.photoFrameStatusHolder = [NSMutableDictionary dictionary];
    self.albumsListHolder = [NSMutableDictionary dictionary];
    self.geoFencingStatusHolder = [NSMutableDictionary dictionary];
    self.photoFrameSection = [NSMutableArray array];
    self.photoFrames = [NSMutableArray arrayWithObjects:self.photoFrameSection, nil];
    
    UINib *cellNib = [UINib nibWithNibName:@"PhotoFrameCell" bundle:nil];
    [self.collectionView registerNib:cellNib forCellWithReuseIdentifier:@"pfCell"];
    
    UICollectionViewFlowLayout *flowLayout = [[UICollectionViewFlowLayout alloc] init];
    [flowLayout setItemSize:CGSizeMake(280, 150)];
    [flowLayout setScrollDirection:UICollectionViewScrollDirectionVertical];
    [self.collectionView setCollectionViewLayout:flowLayout];
    
    AppDelegate *app = [[UIApplication sharedApplication] delegate];
    self.deviceController = app.photoDeviceController;
    self.deviceController.deviceDelegate = self;
    self.deviceController.photoFrameDelegate = self;
    self.deviceController.geoFencingDelegate = self;
    [self waitAttaching:app];
    [self.loadingView startAnimating];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    self.deviceController.deviceDelegate = self;
    self.deviceController.photoFrameDelegate = self;
    self.deviceController.geoFencingDelegate = self;
    if ([self.photoFrameSection count] > 0) {
        for (NSData *key in self.photoFrameSection) {
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
    if (![self.photoFrameSection containsObject:key]) {
        [self.photoFrameSection addObject:key];
    }
    [self.deviceInfoHolder setObject:name forKey:key];

    dispatch_async(dispatch_get_main_queue(), ^{
        [self.loadingView stopAnimating];
        self.loadingView.hidden = true;
        [self.collectionView reloadData];
    });

    [self.deviceController subscribeForUpdatesRequest:endpoinId];
}

- (void)onAlbumsReceived:(NSArray*)albums fromEndpoint:(kaa_endpoint_id_p)endpointId {
    NSData *key = [Utils endpointIdToData:endpointId];
    [self.albumsListHolder setObject:albums forKey:key];

    dispatch_async( dispatch_get_main_queue(), ^{
        [self.collectionView reloadData];
    });
}

- (void)onAlbumStatusUpdate:(PhotoFrameStatus*)status fromEndpoint:(kaa_endpoint_id_p)endpointId {
    NSData *key = [Utils endpointIdToData:endpointId];
    [self.photoFrameStatusHolder setObject:status forKey:key];

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
    return [self.photoFrames count];
}

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return [self.photoFrames[section] count];
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    
    NSMutableArray *data = self.photoFrames[indexPath.section];
    NSString *key = data[indexPath.row];
    PhotoFrameCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"pfCell" forIndexPath:indexPath];
    [cell.layer setCornerRadius:5];

    cell.deviceName.text = [self.deviceInfoHolder objectForKey:key];
    NSString* geoFencingStatus = [self.geoFencingStatusHolder objectForKey:key];
    cell.geoFencingState.text = geoFencingStatus ? geoFencingStatus : @"";
    PhotoFrameStatus *status = [self.photoFrameStatusHolder objectForKey:key];
    cell.thumbnail.image = status.thumbnail;
    cell.noDataLabel.text = cell.thumbnail.image ? @"" : @"No data available";

    cell.thumbnail.hidden = cell.thumbnail.image ? NO : YES;
    cell.mode.text = status.status == ENUM_SLIDE_SHOW_STATUS_PLAYING ? @"Slideshow mode" : @"";
    if (!status || !status.albumId) {
        return cell;
    }

    NSMutableArray *albums = [self.albumsListHolder objectForKey:key];
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

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    NSMutableArray *data = self.photoFrames[indexPath.section];
    NSData *key = data[indexPath.row];
    kaa_endpoint_id_p endpointId = [Utils dataToEndpointId:key];
    NSString *deviceName = [self.deviceInfoHolder objectForKey:key];
    NSMutableArray *albums = [self.albumsListHolder objectForKey:key];

    PhotoAlbumViewController *viewController = [self.storyboard instantiateViewControllerWithIdentifier:@"PhotoAlbumViewController"];
    [viewController setDeviceName:deviceName withAlbums:(NSMutableArray*)albums andEndpointId:endpointId];
    [self presentViewController:viewController animated:YES completion:nil];
}

@end
