//
//  MyHouseViewController.h
//  SmartHouse
//
//  Created by Anton Bohomol on 4/9/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PhotoDeviceController.h"
#import "MusicDeviceController.h"
#import "AppDelegate.h"
#import "PhotoFrameCell.h"
#import "MusicPlayerCell.h"
#import "AlbumsViewController.h"
#import "PhotoAlbumViewController.h"

@interface MyHouseViewController : UIViewController <UICollectionViewDelegate, UICollectionViewDataSource>

@property(weak,nonatomic) IBOutlet UIBarButtonItem *barButton;
@property(weak,nonatomic) IBOutlet UICollectionView *collectionView;
@property(weak,nonatomic) IBOutlet UIActivityIndicatorView *loadingView;

@end