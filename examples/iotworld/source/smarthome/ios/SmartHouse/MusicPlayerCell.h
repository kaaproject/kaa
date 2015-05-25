//
//  MusicPlayerCell.h
//  SmartHouse
//
//  Created by Anton Bohomol on 4/20/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface MusicPlayerCell : UICollectionViewCell

@property(weak,nonatomic) IBOutlet UILabel *deviceName;
@property(weak,nonatomic) IBOutlet UILabel *album;
@property(weak,nonatomic) IBOutlet UILabel *artist;
@property(weak,nonatomic) IBOutlet UILabel *song;
@property(weak,nonatomic) IBOutlet UILabel *state;
@property(weak,nonatomic) IBOutlet UILabel *time;
@property(weak,nonatomic) IBOutlet UIImageView *cover;
@property(weak,nonatomic) IBOutlet UIProgressView *progressBar;
@property(weak,nonatomic) IBOutlet UIImageView *logo;

@property(weak,nonatomic) IBOutlet UILabel *noDataLabel;

@end