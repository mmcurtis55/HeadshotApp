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


class CameraView: UIViewController, UIImagePickerControllerDelegate, UINavigationControllerDelegate{
    
    var captureSession : AVCaptureSession?
    var stillImageOutput : AVCaptureStillImageOutput?
    var previewLayer : AVCaptureVideoPreviewLayer?
    @IBOutlet var cameraView: UIView!
    @IBOutlet var xBtn : UIButton!
    @IBOutlet var saveBtn : UIButton!
    @IBOutlet var shareBtn : UIButton!
    @IBOutlet var flashBtn : UIButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        //x button
        xBtn = UIButton(type: UIButtonType.Custom) as UIButton
        xBtn.setImage(UIImage(named: "X"), forState: .Normal)
        xBtn.frame = CGRectMake(12, 12, 22, 22)
        xBtn.addTarget(self, action: "Xpressed:", forControlEvents: .TouchUpInside)
        
        
        
        let screenSize = UIScreen.mainScreen().bounds
        let screenWidth = screenSize.width
        let screenHeight = screenSize.height
        
        //save button
        saveBtn = UIButton(type: UIButtonType.Custom) as UIButton
        saveBtn.setImage(UIImage(named: "Save"), forState: .Normal)
        saveBtn.frame = CGRectMake(screenWidth - 80, screenHeight - 30, 25, 20)
        saveBtn.addTarget(self, action: "savePhoto:", forControlEvents: .TouchUpInside)
        
        //share button
        shareBtn = UIButton(type: UIButtonType.Custom) as UIButton
        shareBtn.setImage(UIImage(named: "Share"), forState: .Normal)
        shareBtn.frame = CGRectMake(screenWidth - 40, screenHeight - 40, 25 , 30)
        shareBtn.addTarget(self, action: "sharePhoto:", forControlEvents: .TouchUpInside)
        
        
        self.view.addSubview(xBtn)
        self.view.addSubview(saveBtn)
        self.view.addSubview(shareBtn)
    }
    
    func Xpressed(sender: UIButton){
        print("ho boy")
        didPressTakeAnother(true)
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
            print("No camera")
        }
    }
    @IBOutlet var tempImageView: UIImageView!
    
    
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
    
    override func touchesBegan(touches: Set<UITouch>, withEvent event: UIEvent?) {
        print("Touches")
        didPressTakeAnother(false)
    }
    
    
    @IBAction func sharePhoto(sender: AnyObject) {
        
        print("share Photo")
        
        let facebookPost = SLComposeViewController(forServiceType: SLServiceTypeFacebook)
        facebookPost.completionHandler = {
            result in
            switch result {
            case SLComposeViewControllerResult.Cancelled:
                //Code to deal with it being cancelled
                break
                
            case SLComposeViewControllerResult.Done:
                //Code here to deal with it being completed
                break
            }
        }
        
        facebookPost.setInitialText("Test Facebook") //The default text in the tweet
        facebookPost.addImage(tempImageView.image!) //Add an image
        facebookPost.addURL(NSURL(string: "http://facebook.com")) //A url which takes you into safari if tapped on
        
        self.presentViewController(facebookPost, animated: false, completion: {
            //Optional completion statement
        })
    }
    
    
    func savePhoto(sender: UIButton){
        print("saved Photo")
        if didTakePhoto {
            // UIImageWriteToSavedPhotosAlbum(tempImageView.image!, self, "image:didFinishSavingWithError:contextInfo:", nil)
            UIImageWriteToSavedPhotosAlbum(tempImageView.image!, self, nil, nil)
            
        }
        
    }
    
    
    
    
}
