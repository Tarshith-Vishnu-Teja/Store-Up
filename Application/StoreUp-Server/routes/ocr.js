/*
    *
    * Created by Krishna.R.K on 10/25/2017.
    *
*/
var express = require('express');
var router = express.Router(),
    db         = require('./connectdb')(),
    formidable = require('formidable'),
    fs         = require('fs'),
    path       = require('path');

var vision = require('@google-cloud/vision')
var gcloud = vision({
    projectId: 'storeup-164304',

    // The path to your key file:
    keyFilename: 'StoreUp-6dfe8732b53c.json'
});


router.post('/getImageOcr',function (res,req,next) {
    var type = vision.v1.types.Feature.Type.TEXT_DETECTION;
    var featuresElement = { type:type};
    var features = [featuresElement];

    var gcsImageUri = 'gs://storeup-7952a.appspot.com/images/image%3A30.jpg';
    var source = {
        gcsImageUri : gcsImageUri
    };
    var image = {
        content: fs.readFileSync('krk.jpg').toString('base64')
    };
    var requestsElement = {
        image : image,
        features : features
    };

    var requests = [requestsElement];

    gcloud.batchAnnotateImages({requests: requests}).then(function(results) {
        var response = results[0].responses[0].fullTextAnnotation;
        console.log(response);
        // doThingsWith(response)
    })
        .catch(function(err) {
            console.error(err);
        });
    /*vision.detect('public/Images/krk.jpg',types,function (err,detections,apiResponse) {
     if(err){
     console.log(err.message);
     }else{
     console.log(detections);
     }
     })*/
})
module.exports = router;
