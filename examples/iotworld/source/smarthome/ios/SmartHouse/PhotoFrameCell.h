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