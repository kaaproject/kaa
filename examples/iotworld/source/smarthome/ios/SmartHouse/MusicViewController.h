//
//  ViewController.h
//  iotworld
//
//  Created by Anton Bohomol on 4/3/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "SWRevealViewController.h"
#import "MusicDeviceController.h"
#import "AppDelegate.h"

#import "MusicPlayerCell.h"
#import "Utils.h"
#import "AlbumsViewController.h"

@interface MusicViewController : UIViewController <UICollectionViewDelegate, UICollectionViewDataSource>

@property(weak,nonatomic) IBOutlet UIBarButtonItem *barButton;
@property(weak,nonatomic) IBOutlet UICollectionView *collectionView;
@property(weak,nonatomic) IBOutlet UIActivityIndicatorView *loadingView;

@end