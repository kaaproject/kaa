//
//  SongViewCell.h
//  SmartHouse
//
//  Created by Anton Bohomol on 4/19/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface SongViewCell : UITableViewCell

@property(weak,nonatomic) IBOutlet UILabel *songName;
@property(weak,nonatomic) IBOutlet UILabel *duration;

@end
