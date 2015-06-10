//
//  PhotoAlbumViewController.m
//  SmartHouse
//
//  Created by Anton Bohomol on 4/30/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import "PhotoAlbumViewController.h"
#import <AssetsLibrary/AssetsLibrary.h>

@interface PhotoAlbumViewController () <PhotoFrameDelegate, GeoFencingEventDelegate, DeviceEventDelegate>

@property(assign,nonatomic) kaa_endpoint_id_p endpointId;
@property(strong,nonatomic) NSString *deviceName;
@property(strong,nonatomic) NSMutableArray *albums;
@property(strong,nonatomic) NSMutableArray *albumsSection;
@property(strong,nonatomic) PhotoDeviceController *deviceController;
//values to represent current playing item
@property(nonatomic) NSInteger currentAlbumIndex;
@property(strong,nonatomic) PhotoFrameStatus *currentPlayback;
@property(nonatomic) kaa_geo_fencing_event_class_family_operation_mode_t currentMode;
@property(strong,nonatomic) UIActionSheet *mainSheet;

- (void)showGeoFencingActionSheet;
- (void)changeDeviceName;

@end

@implementation PhotoAlbumViewController

- (void)setDeviceName:(NSString *)name withAlbums:(NSMutableArray*)albums andEndpointId:(kaa_endpoint_id_p)endpointId {
    self.deviceName = name;
    self.albumsSection = albums;
    self.endpointId = endpointId;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.imagePicker = [[UIImagePickerController alloc] init];
    //self.imgPicker.allowsEditing = YES;
    self.imagePicker.delegate = self;
    self.imagePicker.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
    
    self.navBar.title = _deviceName;
    self.currentAlbumIndex = -1;
    self.currentPlayback = nil;
    if (!self.albumsSection) {
        self.albumsSection = [[NSMutableArray alloc] init];
    }
    self.albums = [[NSMutableArray alloc] initWithObjects:self.albumsSection, nil];
    
    UINib *cellNib = [UINib nibWithNibName:@"PhotoAlbumCell" bundle:nil];
    [self.collectionView registerNib:cellNib forCellWithReuseIdentifier:@"albumCell"];
    
    UICollectionViewFlowLayout *flowLayout = [[UICollectionViewFlowLayout alloc] init];
    [flowLayout setItemSize:CGSizeMake(145, 165)];
    [flowLayout setScrollDirection:UICollectionViewScrollDirectionVertical];
    [self.collectionView setCollectionViewLayout:flowLayout];
    
    AppDelegate *app = [[UIApplication sharedApplication] delegate];
    self.deviceController = app.photoDeviceController;
    self.deviceController.photoFrameDelegate = self;
    self.deviceController.geoFencingDelegate = self;
    self.deviceController.deviceDelegate = self;
    
    [self.deviceController subscribeForUpdatesRequest:self.endpointId];
    [self.deviceController requestGeoFencingInfo:self.endpointId];
    [self.deviceController albumsRequest:self.endpointId];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    self.deviceController.photoFrameDelegate = self;
    self.deviceController.geoFencingDelegate = self;
    self.deviceController.deviceDelegate = self;
}

- (IBAction)back {
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (void)onAlbumsReceived:(NSArray *)albums fromEndpoint:(kaa_endpoint_id_p)endpointId {
    self.currentAlbumIndex = -1;
    self.currentPlayback = nil;
    [self.albumsSection removeAllObjects];
    [self.albumsSection addObjectsFromArray:albums];
    dispatch_async( dispatch_get_main_queue(), ^{
        [self.collectionView reloadData];
    });
}

- (void)onAlbumStatusUpdate:(PhotoFrameStatus*)status fromEndpoint:(kaa_endpoint_id_p)endpointId {
    NSUInteger albumsLength = [self.albumsSection count];
    for (int i = 0; i < albumsLength; i++) {
        PhotoAlbumInfo *album = self.albumsSection[i];
        if ([album.albumId isEqualToString:status.albumId]) {
            self.currentAlbumIndex = i;
            self.currentPlayback = status;
            dispatch_async( dispatch_get_main_queue(), ^{
                [self.collectionView reloadData];
            });
            break;
        }
    }
}

- (void)foundDevice:(NSString *)name forEndpoint:(kaa_endpoint_id_p)endpoinId {
    dispatch_async( dispatch_get_main_queue(), ^{
        self.deviceName = name;
        self.navBar.title = name;
    });
}

- (void)onStatusReceived:(kaa_geo_fencing_event_class_family_operation_mode_t)mode forEndpoint:(kaa_endpoint_id_p)endpointId {
    self.currentMode = mode;
}

-(NSInteger)numberOfSectionsInCollectionView:(UICollectionView *)collectionView {
    return [self.albums count];
}

-(NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return [self.albums[section] count];
}

-(UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    NSMutableArray *data = self.albums[indexPath.section];
    PhotoAlbumInfo *album = data[indexPath.row];
    
    PhotoAlbumCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"albumCell" forIndexPath:indexPath];
    [cell.layer setCornerRadius:5];
    
    cell.title.text = album.title;
    if (indexPath.row == self.currentAlbumIndex && self.currentPlayback.status == ENUM_SLIDE_SHOW_STATUS_PLAYING) {
        cell.position.text = [NSString stringWithFormat:@"Photo %i of %i", self.currentPlayback.photoNumber, album.size];
        cell.status.hidden = NO;
        cell.thumbnail.image = self.currentPlayback.thumbnail;
    } else {
        cell.thumbnail.image = album.thumbnail;
        cell.status.hidden = YES;
        cell.position.text = [NSString stringWithFormat:@"%i %@", album.size, album.size > 1 ? @"photos" : @"photo"];
    }
    
    return cell;
}

- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info {
    [picker dismissViewControllerAnimated:YES completion:nil];
    NSURL *photoUrl = [info objectForKey:UIImagePickerControllerReferenceURL];
    
    ALAssetsLibraryAssetForURLResultBlock resultblock = ^(ALAsset *imageAsset) {
        ALAssetRepresentation *imageRep = [imageAsset defaultRepresentation];
        Byte *buffer = (Byte*)malloc(imageRep.size);
        NSUInteger buffered = [imageRep getBytes:buffer fromOffset:0.0 length:imageRep.size error:nil];
        NSData *data = [NSData dataWithBytesNoCopy:buffer length:buffered freeWhenDone:YES];
        [self.deviceController uploadPhoto:data withName:[imageRep filename] toEndpoint:self.endpointId];
    };
    
    ALAssetsLibrary* assetslibrary = [[ALAssetsLibrary alloc] init];
    [assetslibrary assetForURL:photoUrl resultBlock:resultblock failureBlock:nil];
}

-(void) imagePickerControllerDidCancel:(UIImagePickerController *)picker{
    [picker dismissViewControllerAnimated:YES completion:nil];
}

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    NSMutableArray *data = self.albums[indexPath.section];
    PhotoAlbumInfo *albumInfo = data[indexPath.row];
    if ([albumInfo.albumId isEqualToString:self.currentPlayback.albumId]
        && self.currentPlayback.status == ENUM_SLIDE_SHOW_STATUS_PLAYING) {
        [self.deviceController pauseSlideshow:self.endpointId];
    } else {
        [self.deviceController startSlideshow:albumInfo.albumId enpoint:self.endpointId];
    }
}

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex {
    if (buttonIndex != alertView.cancelButtonIndex) {
        NSString *name = [[alertView textFieldAtIndex:0] text];
        AppDelegate *app = [[UIApplication sharedApplication] delegate];
        [app.kaaController renameEndpoint:self.endpointId to:name];
    }
}

- (BOOL)alertViewShouldEnableFirstOtherButton:(UIAlertView *)alertView {
    NSString *inputText = [[alertView textFieldAtIndex:0] text];
    return [inputText length] > 0 ? YES : NO;
}

- (void)changeDeviceName {
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"New device name:"
                                                      message:nil
                                                     delegate:self
                                            cancelButtonTitle:@"Cancel"
                                            otherButtonTitles:@"Rename", nil];
    
    [alert setAlertViewStyle:UIAlertViewStylePlainTextInput];
    [alert textFieldAtIndex:0].text = self.deviceName;
    [alert show];
}

- (IBAction)settings:(id)sender {
    self.mainSheet = [[UIActionSheet alloc] initWithTitle:nil
                                                 delegate:self
                                        cancelButtonTitle:nil
                                   destructiveButtonTitle:nil
                                        otherButtonTitles:@"Upload photo", @"Delete all uploaded photos", @"Rename device", @"Geo fencing", nil];
    [self.mainSheet addButtonWithTitle:@"Cancel"];
    [self.mainSheet showInView:self.view];
}

- (void)showGeoFencingActionSheet {
    UIActionSheet *geoFencingSheet = [[UIActionSheet alloc] initWithTitle:nil
                                                                 delegate:self
                                                        cancelButtonTitle:nil
                                                   destructiveButtonTitle:nil
                                                        otherButtonTitles:nil];
    NSArray *modes = [Utils geoFencingModesWithCurrent:self.currentMode];
    for (NSString *mode in modes) {
        [geoFencingSheet addButtonWithTitle:mode];
    }
    [geoFencingSheet addButtonWithTitle:@"Cancel"];
    [geoFencingSheet showInView:self.view];
}

- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
    if (actionSheet == self.mainSheet) {
        switch (buttonIndex) {
            case 0:
                [self presentViewController:self.imagePicker animated:YES completion:nil];
                break;
            case 1:
                [self.deviceController deletePhotos:self.endpointId];
                break;
            case 2:
                [self changeDeviceName];
                break;
            case 3:
                [self showGeoFencingActionSheet];
                break;
        }
    } else {
        //we rely on fact that we have 3 buttons and according to that 3 modes which values are
        //exactly that same as button indexes
        if (buttonIndex < 3) {
            [self.deviceController changeGeoFencingMode:buttonIndex forEndpoint:self.endpointId];
            //update current mode manually so we wouldn't wait for response
            self.currentMode = buttonIndex;
        }
    }
}

- (void)willPresentActionSheet:(UIActionSheet *)actionSheet {
    for (UIView *subview in actionSheet.subviews) {
        if ([subview isKindOfClass:[UIButton class]]) {
            UIButton *button = (UIButton *)subview;
            [button setTitleColor:[UIColor colorWithRed:111.0/255.0 green:134.0/255.0 blue:146.0/255.0 alpha:1.0] forState:UIControlStateNormal];
        }
    }
}

@end
