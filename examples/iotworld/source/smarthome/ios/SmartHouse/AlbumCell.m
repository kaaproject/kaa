//
//  AlbumCell.m
//  SmartHouse
//
//  Created by Anton Bohomol on 4/19/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import "AlbumCell.h"

@implementation AlbumCell

- (void)awakeFromNib {
    self.cover.layer.masksToBounds = YES;
    self.cover.layer.borderColor = [UIColor whiteColor].CGColor;
    self.cover.layer.borderWidth = 1;
}

@end
