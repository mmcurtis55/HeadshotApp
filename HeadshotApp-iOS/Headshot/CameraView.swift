//
//  CameraView.swift
//  Headshot
//
//  Created by Curtis on 11/11/15.
//
//

import UIKit
import AVFoundation
import Social
import CWStatusBarNotification

enum Flash {
    case Flash
    case NoFlash
}

class CameraView: UIViewController, UIImagePickerControllerDelegate, UINavigationControllerDelegate{
    
    var captureSession : AVCaptureSession?
    var stillImageOutput : AVCaptureStillImageOutput?
    var previewLayer : AVCaptureVideoPreviewLayer?
    //dynamic image display
    @IBOutlet var cameraView: UIView!
    
    // Buttons
    @IBOutlet var xBtn : UIButton!
    @IBOutlet var saveBtn : UIButton!
    @IBOutlet var shareBtn : UIButton!
    @IBOutlet var flashBtn : UIButton!
    @IBOutlet var captureBtn : UIButton!
    
    var isFlash:Bool = true
    
    
    //for saved notification
     let notification = CWStatusBarNotification()
    let defaults = NSUserDefaults.standardUserDefaults()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        xBtn.hidden = true
        saveBtn.hidden = true
        saveBtn.hidden = true
        shareBtn.hidden = true
        flashBtn.hidden = false
        captureBtn.hidden = false
        
        isFlash = defaults.boolForKey("flashState")
        
        
        


    }
    

    
     @IBAction func flashPressed(sender: UIButton){

        if(isFlash){
            flashBtn.setImage(UIImage(named: "flash_off"), forState: .Normal)
            defaults.setBool(false, forKey: "flashState")

        }else{
            flashBtn.setImage(UIImage(named: "flash_on"), forState: .Normal)
            defaults.setBool(true, forKey: "flashState")
        }
        
        
    }
    
    @IBAction func xPressed(sender: UIButton){
        print("Release")
        xBtn.hidden = true
        saveBtn.hidden = true
        saveBtn.hidden = true
        shareBtn.hidden = true
        flashBtn.hidden = false
        captureBtn.hidden = false
        didPressTakeAnother(true)
    }
    
    @IBAction func capturePressed(sender: UIButton){
        print("Capture")
        xBtn.hidden = false
        saveBtn.hidden = false
        saveBtn.hidden = false
        shareBtn.hidden = false
        flashBtn.hidden = true
        captureBtn.hidden = true
        didPressTakeAnother(false)
    }
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        previewLayer?.frame = cameraView.bounds
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        captureSession = AVCaptureSession()
        //captureSession?.sessionPreset = AVCaptureSessionPreset1920x1080
        captureSession?.sessionPreset = AVCaptureSessionPreset1280x720
        
        //let backCamera = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
        
        
        let videoDevices = AVCaptureDevice.devicesWithMediaType(AVMediaTypeVideo)
        if (AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo) != nil){
         var captureDevice:AVCaptureDevice  = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
        
        for device in videoDevices{
            let device = device as! AVCaptureDevice
            if device.position == AVCaptureDevicePosition.Front {
                captureDevice = device
                break
            }
        }
        
        
        var error : NSError?
        var input: AVCaptureDeviceInput!
        do {
            input = try AVCaptureDeviceInput(device: captureDevice)
        } catch let error1 as NSError {
            error = error1
            input = nil
        }
        
        if (error == nil && captureSession?.canAddInput(input) != nil){
            
            captureSession?.addInput(input)
            
            stillImageOutput = AVCaptureStillImageOutput()
            stillImageOutput?.outputSettings = [AVVideoCodecKey : AVVideoCodecJPEG]
            
            if (captureSession?.canAddOutput(stillImageOutput) != nil){
                captureSession?.addOutput(stillImageOutput)
                
                previewLayer = AVCaptureVideoPreviewLayer(session: captureSession)
                previewLayer?.videoGravity = AVLayerVideoGravityResizeAspect
                previewLayer?.connection.videoOrientation = AVCaptureVideoOrientation.Portrait
                cameraView.layer.addSublayer(previewLayer!)
                captureSession?.startRunning()
                
            }
            
            
        }
        
        }else{
            tempImageView.image = UIImage(named: "iTunesArtwork.png")
            print("No camera")
        }
    }
    
    //still image
    @IBOutlet var tempImageView: UIImageView!
    
    //image + overlay combo
     var composit: UIImage!
    
    
    func didPressTakePhoto(){
        
        if let videoConnection = stillImageOutput?.connectionWithMediaType(AVMediaTypeVideo){
            videoConnection.videoOrientation = AVCaptureVideoOrientation.Portrait
            stillImageOutput?.captureStillImageAsynchronouslyFromConnection(videoConnection, completionHandler: {
                (sampleBuffer, error) in
                
                if sampleBuffer != nil {
                    
                    
                    let imageData = AVCaptureStillImageOutput.jpegStillImageNSDataRepresentation(sampleBuffer)
                    let dataProvider  = CGDataProviderCreateWithCFData(imageData)
                    let cgImageRef = CGImageCreateWithJPEGDataProvider(dataProvider, nil, true, CGColorRenderingIntent.RenderingIntentDefault)
                    
                    let image = UIImage(CGImage: cgImageRef!, scale: 1.0, orientation: UIImageOrientation.LeftMirrored)
                    
                    self.tempImageView.image = image
                    self.tempImageView.hidden = false
                    
                            let bottomImage = self.tempImageView.image!
                            let topImage = UIImage(named: "SuitND.png")
                    
                    let screenSize: CGRect = UIScreen.mainScreen().bounds
                    
                    let screenWidth = screenSize.width
                    let screenHeight = screenSize.height
                    
                            let size = CGSize(width: screenWidth, height: screenHeight)
                            UIGraphicsBeginImageContext(size)
                    
                            let areaSize = CGRect(x: 0, y: 0, width: size.width, height: size.height)
                            bottomImage.drawInRect(areaSize)
                    
                            topImage!.drawInRect(areaSize, blendMode: .Normal, alpha: 1.0)
                            
                            
                            self.composit = UIGraphicsGetImageFromCurrentImageContext()
                            UIGraphicsEndImageContext()
                    
                }
                
                
            })
        }
        

    }
    
    
    var didTakePhoto = Bool()
    
    func didPressTakeAnother(x :Bool){
        
        
        if didTakePhoto == true && x {  // if x is pressed and if the photo is displayed as still image
            print("tap 1")
            tempImageView.hidden = true
            didTakePhoto = false
            
        }else if !didTakePhoto && !x { // if main screen was pressed and if the screen is dynamic video
            print("tap 2")
            captureSession?.startRunning()
            didTakePhoto = true
            didPressTakePhoto()
            
        }
        
    }
    
    
    
    //Shares photo and text
    @IBAction func sharePhoto(sender: UIButton) {
        
        print("share Photo")
        
        
            let textToShare = "Check out the Headshot app in the App Store"
        
            if let image = composit
            {
                //image = UIImage(CGImage: image.CGImage!, scale: 1.0, orientation: .RightMirrored)
                let objectsToShare = [textToShare, image]
                let activityVC = UIActivityViewController(activityItems: objectsToShare, applicationActivities: nil)
                
                //New Excluded Activities Code
                activityVC.excludedActivityTypes = [UIActivityTypeAirDrop, UIActivityTypeAddToReadingList]
                //
                
                self.presentViewController(activityVC, animated: true, completion: nil)
            }else{
            print("no image to save")
        }
        
    }
    
    //Saves composit to camera roll
    @IBAction func savePhoto(sender: UIButton){
        print("saved Photo")
        setupNotification()
        self.notification.notificationLabelBackgroundColor = UIColor(red: 0.0,
            green: 122.0/255.0, blue: 0.5, alpha: 1.0)
        self.notification.displayNotificationWithMessage("Photo Saved", forDuration: 1.0)
        

        if let image = composit {
            UIImageWriteToSavedPhotosAlbum(image, self, "image:didFinishSavingWithError:contextInfo:", nil)

        } else { print("Image save error: No image to save") }
        
    }
    
    func image(image: UIImage, didFinishSavingWithError error: NSError?, contextInfo:UnsafePointer<Void>) {
        if error == nil {
            
        }
        else
        {
            print("Error saving Image")
            //log the error out here ,if any
        }
    }
    
    func setupNotification() {
        guard let inStyle = CWNotificationAnimationStyle(rawValue:
           0) else {
                return
        }
        guard let outStyle = CWNotificationAnimationStyle(rawValue:
            0) else {
                return
        }
        guard let notificationStyle = CWNotificationStyle(rawValue:
            0) else {
                return
        }
        self.notification.notificationAnimationInStyle = inStyle
        self.notification.notificationAnimationOutStyle = outStyle
        self.notification.notificationStyle = notificationStyle
    }


    
}
