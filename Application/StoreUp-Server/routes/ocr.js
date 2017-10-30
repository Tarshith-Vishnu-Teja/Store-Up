/**
 * Created by Krishna.R.K on 10/28/2017.
 */

var express = require('express');
var router = express.Router(),
    db         = require('./connectdb')(),
    formidable = require('formidable'),
    fs         = require('fs-extra'),
    path       = require('path');

var vision = require('@google-cloud/vision')
var gcloud = vision({
    projectId: 'storeup-164304',

    // The path to your key file:
    keyFilename: '../ServiceAccountKey/StoreUp-6dfe8732b53c.json'
});


var googleMapsClient = require('@google/maps').createClient({
    key:'AIzaSyCLwIbvlz-UN3Z-E5nUgolJGCPLwnhcnHo'
});

router.post('/getImageOcr', function(req, res, next) {
    var StorageReference = req.body.StorageReference;

   console.log(StorageReference);

    var gcsImageUri = 'gs://storeup-7952a.appspot.com/images/'+StorageReference;
    var source = {
        gcsImageUri : gcsImageUri
    };
    var image = {
        source : source
    };
    // var image = {
    // content: fs.readFileSync('../public/Images/Safeway.jpg').toString('base64')
    // };
    var type = vision.v1.types.Feature.Type.TEXT_DETECTION;
    var logoType = vision.v1.types.Feature.Type.LOGO_DETECTION;

    var featuresElement = { type:type};
    var featuresElementLogo = { type:logoType};
    var features = [featuresElement,featuresElementLogo];
    /*var featuresElement = {
        type : type
    };
    var features = [featuresElement];*/
    var requestsElement = {
        image : image,
        features : features
    };
    var requests = [requestsElement];
    gcloud.batchAnnotateImages({requests: requests}).then(function(results) {
        /*var response = responses[0];
        console.log(JSON.stringify(response));*/


        var text = results[0].responses[0].fullTextAnnotation.text;
        console.log("Text is: \n"+JSON.stringify(text));
        var logo="";
        if(typeof (results[0].responses[0].logoAnnotations[0])=="undefined"){
            var count=0;
            for(var i=0;i<text.length;i++){
                if(count>=1){
                    break;
                }
                if(count==0){
                    logo+=text.charAt(i);
                }
                if(text.charAt(i)=="\n")
                    count++;
            }
        }else{
            logo = results[0].responses[0].logoAnnotations[0].description;
        }

        var address="";
        //console.log("Text is: \n"+text);
        //console.log("Logo is: \n"+logo);
        if(logo=="Target" || logo=="Target Corporation"){
            var count=0;
            for(var i=0;i<text.length;i++){
                if(count>1){
                    break;
                }
                if(count==1){
                    address+=text.charAt(i);
                }
                if(text.charAt(i)=='\n'){
                    count++;
                }
            }
            console.log(address);
        }else if(logo == "Walmart"){
            var count=0;
            for(var i=0;i<text.length;i++){
                if(count>5){
                    break;
                }
                if(count==5){
                    address+=text.charAt(i);
                }
                if(text.charAt(i)=='\n'){
                    count++;
                }
            }
        }else if (logo == "Costco"){
            var count=0;
            for(var i=0;i<text.length;i++){
                if(count>3){
                    break;
                }
                if(count==1 || count==2){
                    address+=text.charAt(i);
                }
                if(text.charAt(i)=='\n'){
                    count++;
                }
            }
        }else if  (logo == "SAFEWAY"){

        }

        /*console.log("Text is: \n"+text);
         console.log("Store Name is :"+logo+" and the address is:"+address);*/
        var addr = logo+" "+address;
        console.log(addr);

        googleMapsClient.geocode({
            address: addr
        },function (err,response) {
            if(!err){
                console.log(response.json.results[0].address_components);
            }
        });



        // doThingsWith(response)
    }).catch(function(err) {
        console.error(err);
    });


});
module.exports=router;