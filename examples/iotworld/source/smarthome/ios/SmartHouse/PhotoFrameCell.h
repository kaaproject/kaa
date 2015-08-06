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
//  PhotoFrameCell.h
//  SmartHouse
//
//  Created by Anton Bohomol on 4/28/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface PhotoFrameCell : UICollectionViewCell

@property(weak,nonatomic) IBOutlet UILabel *deviceName;
@property(weak,nonatomic) IBOutlet UILabel *album;
@property(weak,nonatomic) IBOutlet UILabel *photoPosition;
@property(weak,nonatomic) IBOutlet UILabel *mode;
@property(weak,nonatomic) IBOutlet UILabel *geoFencingState;
@property(weak,nonatomic) IBOutlet UIImageView *thumbnail;
@property(weak,nonatomic) IBOutlet UILabel *noDataLabel;

@end