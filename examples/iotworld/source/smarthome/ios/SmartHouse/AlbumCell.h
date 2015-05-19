//
//  AlbumCell.h
//  SmartHouse
//
//  Created by Anton Bohomol on 4/19/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface AlbumCell : UICollectionViewCell

@property (weak,nonatomic) IBOutlet UILabel *album;
@property (weak,nonatomic) IBOutlet UILabel *artist;
@property (weak,nonatomic) IBOutlet UILabel *tracks;
@property (weak,nonatomic) IBOutlet UILabel *song;
@property (weak,nonatomic) IBOutlet UILabel *time;
@property (weak,nonatomic) IBOutlet UIImageView *cover;
@property (weak,nonatomic) IBOutlet UIProgressView *progressBar;

@end
