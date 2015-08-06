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
//  AlbumsViewController.h
//  SmartHouse
//
//  Created by Anton Bohomol on 4/19/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MusicViewController.h"
#import "MusicPlayer.h"
#import "AlbumCell.h"
#import "Utils.h"
#import "MusicDeviceController.h"

@interface AlbumsViewController : UIViewController <UICollectionViewDelegate, UICollectionViewDataSource, UIActionSheetDelegate, UIAlertViewDelegate>

@property(weak,nonatomic) IBOutlet UIBarButtonItem *barButton;
@property(weak,nonatomic) IBOutlet UICollectionView *collectionView;
@property(weak,nonatomic) IBOutlet UISlider *volumeSlider;
@property(weak,nonatomic) IBOutlet UINavigationItem *navBar;

- (void)setDeviceName:(NSString *)name withAlbums:(NSMutableArray*)albums andEndpointId:(kaa_endpoint_id_p)endpointId;
- (IBAction)volumeChanged:(id)sender;
- (IBAction)back;

@end
