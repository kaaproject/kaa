//
//  AlbumsViewController.h
//  SmartHouse
//
//  Created by Anton Bohomol on 4/19/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MusicViewController.h"
#import "MusicPlayer.h"
#import "AlbumCell.h"
#import "Utils.h"
#import "MusicDeviceController.h"

@interface AlbumsViewController : UIViewController <UICollectionViewDelegate, UICollectionViewDataSource, UIActionSheetDelegate, UIAlertViewDelegate>

@property(weak,nonatomic) IBOutlet UIBarButtonItem *barButton;
@property(weak,nonatomic) IBOutlet UICollectionView *collectionView;
@property(weak,nonatomic) IBOutlet UISlider *volumeSlider;
@property(weak,nonatomic) IBOutlet UINavigationItem *navBar;

- (void)setDeviceName:(NSString *)name withAlbums:(NSMutableArray*)albums andEndpointId:(kaa_endpoint_id_p)endpointId;
- (IBAction)volumeChanged:(id)sender;
- (IBAction)back;

@end
