//
//  PhotoAlbumCell.h
//  SmartHouse
//
//  Created by Anton Bohomol on 4/28/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface PhotoAlbumCell : UICollectionViewCell

@property (weak,nonatomic) IBOutlet UIImageView *thumbnail;
@property (weak,nonatomic) IBOutlet UILabel *title;
@property (weak,nonatomic) IBOutlet UILabel *position;
@property (weak,nonatomic) IBOutlet UIImageView *status;

@end