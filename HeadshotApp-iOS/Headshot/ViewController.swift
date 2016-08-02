//
//  ViewController.swift
//  Headshot
//
//  Created by Curtis on 11/10/15.
//
//

import UIKit
import AVFoundation
import Social

enum ShareError: ErrorType {
    case ShareFail
    case Other
}

class ViewController: UIViewController, UIImagePickerControllerDelegate, UINavigationControllerDelegate, UIScrollViewDelegate {
    
    @IBOutlet var scrollView: UIScrollView!
    

    
    var pageImages: [UIImage] = []
    var pageViews: [UIImageView?] = []
    var currentPage: Int?
    
    
    var captureSession : AVCaptureSession?
    var stillImageOutput : AVCaptureStillImageOutput?
    var previewLayer : AVCaptureVideoPreviewLayer?
    //dynamic image display
    @IBOutlet var cameraView: UIView!
    
    // Buttons
    @IBOutlet var xBtn : UIButton!
    @IBOutlet var saveBtn : UIButton!
    @IBOutlet var shareBtn : UIButton!
    
    @IBOutlet var captureBtn : UIButton!
    
    
    var isFlash:Bool = true
    
    
    let defaults = NSUserDefaults.standardUserDefaults()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        
        //scrollView
        let screenSize: CGRect = UIScreen.mainScreen().bounds
        
        let screenWidth = screenSize.width
        let screenHeight = screenSize.height
        
        scrollView.bounds = CGRect(x: 0, y: 0, width: screenWidth, height: screenHeight)
        
        
        // 1
        pageImages = [UIImage(named:"CutOut1.png")!,
                      UIImage(named:"CutOut2.png")!,
                      UIImage(named:"CutOut3.png")!]
        //currentPage = 0
        
        let pageCount = pageImages.count
        
        // 3
        for _ in 0..<pageCount {
            pageViews.append(nil)
        }
        
        // 4
        
        
        let pagesScrollViewSize = scrollView.frame.size
        scrollView.contentSize = CGSizeMake(screenSize.width * CGFloat(pageImages.count), pagesScrollViewSize.height)
        
        
        // 5
        loadVisiblePages()
        
        xBtn.hidden = true
        saveBtn.hidden = true
        saveBtn.hidden = true
        shareBtn.hidden = true
        
        captureBtn.hidden = false
        
        
        isFlash = defaults.boolForKey("flashState")
        
        
        
        
        
    }
    
    
    @IBAction func xPressed(sender: UIButton){
        //print("Release")
        xBtn.hidden = true
        saveBtn.hidden = true
        saveBtn.hidden = true
        shareBtn.hidden = true
        captureBtn.hidden = false
        scrollView.scrollEnabled = true
        
        didPressTakeAnother(true)
    }
    
    @IBAction func capturePressed(sender: UIButton){
        //print("Capture")
        xBtn.hidden = false
        saveBtn.hidden = false
        saveBtn.hidden = false
        shareBtn.hidden = false
        captureBtn.hidden = true
        scrollView.scrollEnabled = false
        didPressTakeAnother(false)
    }
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        print("MEMWarning")
        // Dispose of any resources that can be recreated.
    }
    
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        //self.defaults.setValue("true", forKey: "show")
        previewLayer?.frame = cameraView.bounds
        if let show = defaults.stringForKey("show") {
            if show == "true"{
                beginingPopup()
            } else if show == "share"{
            defaults.setValue("true", forKey: "show")
                }
        }else {
            //Nothing stored in NSUserDefaults yet. Set a value.
            defaults.setValue("true", forKey: "show")
            beginingPopup()
        }

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
            tempImageView.image = UIImage(named: "tom_brady.png")
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
                    
                    autoreleasepool {
                        self.composit = nil
                        self.createComposit()
                    }
                    
                }
                
                
            })
        }
        
        
    }
    
    
    func createComposit(){
        autoreleasepool {
        let bottomImage = self.tempImageView.image!
        

        let width: CGFloat = scrollView.frame.size.width;
        let num:CGFloat = (0.5 * width)
        let page = NSInteger((num + scrollView.contentOffset.x) / width);
        let topImage = self.pageImages[page]
        
        let screenSize: CGRect = UIScreen.mainScreen().bounds
        
        let screenWidth = screenSize.width
        let screenHeight = screenSize.height
        
        let size = CGSize(width: screenWidth, height: screenHeight)
        UIGraphicsBeginImageContext(size)
        
        let areaSize = CGRect(x: 0, y: 0, width: size.width, height: size.height)
        bottomImage.drawInRect(areaSize)
        
        topImage.drawInRect(areaSize, blendMode: .Normal, alpha: 1.0)
        
        
        self.composit = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        }
    }
    
    
    var didTakePhoto = Bool()
    
    func didPressTakeAnother(x :Bool){
        
        
        if didTakePhoto == true && x {  // if x is pressed and if the photo is displayed as still image
           // print("tap 1")
            tempImageView.hidden = true
            didTakePhoto = false
            
        }else if !didTakePhoto && !x { // if main screen was pressed and if the screen is dynamic video
            ///print("tap 2")
            captureSession?.startRunning()
            didTakePhoto = true
            didPressTakePhoto()
            
        }
        
    }
    
    
    //Shares photo and text
    @IBAction func sharePhoto(sender: UIButton) {
        
        //let textToShare = "Check out the Headshot app in the App Store"
        autoreleasepool{
       
        if let image = composit
        {
            
            guard let shrImg:Optional = [image as UIImage] else {
                print("ERROR!")
                return
            }
            
            let activityVC :UIActivityViewController = UIActivityViewController(activityItems: shrImg!, applicationActivities: nil)
            activityVC.excludedActivityTypes = [UIActivityTypeAddToReadingList, UIActivityTypeMail, UIActivityTypePostToVimeo]
            self.presentViewController(activityVC, animated: true, completion:nil)
            

            if let _ = self.defaults.stringForKey("show") {
                self.defaults.setValue("share", forKey: "show")
            }else{
                
                //Nothing stored in NSUserDefaults yet. Set a value.
                self.defaults.setValue("share", forKey: "show")
            }

        }else{
            print("no image to save")
        }
        }
        
    }
    
    


    
    //Saves composit to camera roll
    @IBAction func savePhoto(sender: UIButton){
        //print("saved Photo")
        
        autoreleasepool{
        
        if let image = composit {
            UIImageWriteToSavedPhotosAlbum(image, self, #selector(ViewController.image(_:didFinishSavingWithError:contextInfo:)), nil)
            
            /*let alert = UIAlertController(title: "Saved", message:
                "Headshot saved", preferredStyle: .Alert) // 1
            let firstAction = UIAlertAction(title: "OK", style: .Default) { (alert: UIAlertAction!) -> Void in
                //NSLog("You pressed button one")
            } // 2
            alert.addAction(firstAction) // 4
            presentViewController(alert, animated: true, completion:nil) // 6*/
            
        } else { NSLog("Image save error: No image to save") }
        }
        
    }
    
    func image(image: UIImage, didFinishSavingWithError error: NSError?, contextInfo:UnsafePointer<Void>) {
        if error == nil {
            
        }
        else
        {
            NSLog("Error saving Image")
            //log the error out here ,if any
        }
        
    }
    
    
    
    
    
    override func prefersStatusBarHidden() -> Bool {
        return true
        
    }
    
    func loadPage(page: Int) {
        
        if page < 0 || page >= pageImages.count {
            // If it's outside the range of what you have to display, then do nothing
            return
        }
        
        // 1
        if let _ = pageViews[page] {
            // Do nothing. The view is already loaded.
        } else {
            // 2
            var frame = scrollView.bounds
            frame.origin.x = frame.size.width * CGFloat(page)
            frame.origin.y = 0.0
            
            // 3
            let newPageView = UIImageView(image: pageImages[page])
            newPageView.contentMode = .ScaleAspectFit
            newPageView.frame = frame
            scrollView.addSubview(newPageView)
            
            // 4
            pageViews[page] = newPageView
        }
    }
    
    func purgePage(page: Int) {
        
        if page < 0 || page >= pageImages.count {
            // If it's outside the range of what you have to display, then do nothing
            return
        }
        
        // Remove a page from the scroll view and reset the container array
        if let pageView = pageViews[page] {
            pageView.removeFromSuperview()
            pageViews[page] = nil
        }
        
    }
    
    /*func loadVisiblePages() {
        NSLog("load load ")
        // First, determine which page is currently visible
        let pageWidth = scrollView.frame.size.width
        let page = Int(floor((scrollView.contentOffset.x * 2.0 + pageWidth) / (pageWidth * 2.0)))
        
        
        // Update the page control
        currentPage = page
        
        // Work out which pages you want to load
        let firstPage = page - 1;
        let lastPage = page + 1
        
        
        // Purge anything before the first page
        for index in -1 ... firstPage {
            purgePage(index)
        }
        
        // Load pages in our range
        for index in firstPage ... lastPage {
            loadPage(index)
        }
        
        // Purge anything after the last page
        for index in lastPage ... pageImages.count {
            purgePage(index)
        }
    }*/
 
 
 func loadVisiblePages() {
 
         // First, determine which page is currently visible
        let pageWidth = scrollView.frame.size.width
        let page = Int(floor((scrollView.contentOffset.x * 2.0 + pageWidth) / (pageWidth * 2.0)))
 
         // Update the page control
        //print(page)
         currentPage = page
 
         // Work out which pages you want to load
         let firstPage = page - 1
         let lastPage = page + 2
 
 
         // Purge anything before the first page
         for var index = 0; index < firstPage; ++index {
             purgePage(index)
         }
 
         // Load pages in our range
         for var index = firstPage; index <= lastPage; ++index {
             loadPage(index)
        }
 
        // Purge anything after the last page
         for var index = lastPage+1; index < pageImages.count; ++index {
             purgePage(index)
         }
     }
    
    func beginingPopup(){
        if let show = self.defaults.stringForKey("show")  {
            if( show == "true"){
                
                let alert = UIAlertController(title: "How To", message:
                    "Swipe left or right on the top 80% of the screen to change the cutout.\n\nTap the bottom 20% of the screen to capture your Headshot.", preferredStyle: .Alert) // 1
                let firstAction = UIAlertAction(title: "OK", style: .Default) { (alert: UIAlertAction!) -> Void in
                    
                    if let _ = self.defaults.stringForKey("show") {
                        self.defaults.setValue("true", forKey: "show")
                    }else{
                        
                        //Nothing stored in NSUserDefaults yet. Set a value.
                        self.defaults.setValue("true", forKey: "show")
                    }
                    
                    //NSLog("You pressed button one")
                } // 2
                
                let secondAction = UIAlertAction(title: "OK and Don't Show Again", style: .Default) { (alert: UIAlertAction!) -> Void in
                    
                    if let _ = self.defaults.stringForKey("show") {
                        self.defaults.setValue("false", forKey: "show")
                    }else{
                        
                        //Nothing stored in NSUserDefaults yet. Set a value.
                        self.defaults.setValue("false", forKey: "show")
                    }
                    
                   // NSLog("You pressed button two")
                } // 3
                
                alert.addAction(firstAction) // 4
                alert.addAction(secondAction) // 5
                presentViewController(alert, animated: true, completion:nil) // 6
            }
            
        }else{

            
            self.defaults.setValue("true", forKey: "show")
        }
        
        }
    
    
    
    func scrollViewDidScroll(scrollView: UIScrollView) {
        // Load the pages that are now on screen
        loadVisiblePages()
    }
    
    
    
}
