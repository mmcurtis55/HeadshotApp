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
    @IBOutlet var captureBtn : UIButton!
    @IBOutlet var savedBan : UIImageView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        xBtn.hidden = true
        saveBtn.hidden = true
        saveBtn.hidden = true
        shareBtn.hidden = true
        flashBtn.hidden = false
        captureBtn.hidden = false
        savedBan.hidden = true

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
    
//    override func touchesBegan(touches: Set<UITouch>, withEvent event: UIEvent?) {
//        print("Touches")
//        didPressTakeAnother(false)
//    }
    
    
    @IBAction func sharePhoto(sender: UIButton) {
        
        print("share Photo")
        
        
            let textToShare = "Check out the Headshot app in the App Store"
        
            if var image = tempImageView.image
            {
                image = UIImage(CGImage: image.CGImage!, scale: 1.0, orientation: .RightMirrored)
                let objectsToShare = [textToShare, image]
                let activityVC = UIActivityViewController(activityItems: objectsToShare, applicationActivities: nil)
                
                //New Excluded Activities Code
                activityVC.excludedActivityTypes = [UIActivityTypeAirDrop, UIActivityTypeAddToReadingList]
                //
                
                self.presentViewController(activityVC, animated: true, completion: nil)
            }
        
    }
    
    
    @IBAction func savePhoto(sender: UIButton){
        print("saved Photo")
        savedBan.hidden = false

        if let image = tempImageView.image {
            UIImageWriteToSavedPhotosAlbum(image, self, "image:didFinishSavingWithError:contextInfo:", nil)

        } else { print("some error message") }
        
    }
    
    func image(image: UIImage, didFinishSavingWithError error: NSError?, contextInfo:UnsafePointer<Void>) {
        if error == nil {
            
        }
        else
        {
            //log the error out here ,if any
        }
    }
    
}
