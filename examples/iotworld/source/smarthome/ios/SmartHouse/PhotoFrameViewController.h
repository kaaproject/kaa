//
//  PhotoFrameViewController.h
//  SmartHouse
//
//  Created by Anton Bohomol on 4/6/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PhotoDeviceController.h"
#import "AppDelegate.h"
#import "PhotoFrameCell.h"
#import "PhotoAlbumViewController.h"

@interface PhotoFrameViewController : UIViewController <UICollectionViewDelegate, UICollectionViewDataSource>

@property(weak,nonatomic) IBOutlet UIBarButtonItem *barButton;
@property(weak,nonatomic) IBOutlet UICollectionView *collectionView;
@property(weak,nonatomic) IBOutlet UIActivityIndicatorView *loadingView;

@end
