//
//  MusicViewHolder.m
//  SmartHouse
//
//  Created by Anton Bohomol on 4/15/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@interface MusicViewHolder : UIViewController

@property(strong, nonatomic)NSMutableArray* items;

@end

@implementation MusicViewHolder

- (void)viewDidLoad {
    [super viewDidLoad];
    _items = [NSArray mutableCopy];
    [_items addObject:@"first"];
    [_items addObject:@"second"];
    [_items addObject:@"third"];
    [_items addObject:@"fourth"];
    [_items addObject:@"fifth"];
}

@end
