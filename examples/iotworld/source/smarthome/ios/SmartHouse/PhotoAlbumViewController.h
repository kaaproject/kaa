//
//  PhotoAlbumViewController.h
//  SmartHouse
//
//  Created by Anton Bohomol on 4/30/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PhotoDeviceController.h"
#import "PhotoFrameStatus.h"
#import "AppDelegate.h"
#import "PhotoAlbumCell.h"

@interface PhotoAlbumViewController : UIViewController <UIImagePickerControllerDelegate, UINavigationControllerDelegate, UICollectionViewDelegate, UICollectionViewDataSource, UIActionSheetDelegate, UIAlertViewDelegate>

@property(weak,nonatomic) IBOutlet UICollectionView *collectionView;
@property(weak,nonatomic) IBOutlet UINavigationItem *navBar;
@property(retain,nonatomic) UIImagePickerController *imagePicker;

- (void)setDeviceName:(NSString *)name withAlbums:(NSMutableArray*)albums andEndpointId:(kaa_endpoint_id_p)endpointId;
- (IBAction)back;

@end
