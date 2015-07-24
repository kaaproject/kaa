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
