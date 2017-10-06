/**
 * Created by Krishna.R.K on 10/5/2017.
 */
var express = require('express');
var router = express.Router(),
    db         = require('./connectdb')(),
    formidable = require('formidable'),
    fs         = require('fs-extra'),
    path       = require('path');


router.post('/login', function(req, res, next) {
    var tag      = req.body.tag,
        email    = req.body.email,
        password = req.body.password;
    console.log(email);
    console.log(password);
    console.log(tag);


    db.query('select * from user_details where email = ? and password = ?', [email, password], function(err, rows, fields) {
        if (err) throw err;

        if (rows.length > 0) {
            res.json({success: "1", username: rows[0].user_name, message: "user logged in"});
        }else {
            res.json({success: "0", message: "invalid email or password"});
        }
    })
});
router.post('/register',function (req, res, next) {
    var tag      = req.body.tag,
        username = req.body.username,
        email    = req.body.email,
        password = req.body.password,
        street = req.body.street,
        city = req.body.city,
        state = req.body.state,
        zipcode = req.body.zipcode,
        phone = req.body.phone;

    console.log(email);
    console.log(password);
    console.log(street);
    console.log(city);
    console.log(username);
    console.log(state);
    console.log(zipcode);
    console.log(phone);

    //check whether user exists
    db.query('select * from user_details where email = ?', [email], function(err, rows, result) {
        if (err) throw err;
        if(rows.length > 0){
            res.json({success: "0", message: "email exists"});
        }else {
            //check username exists
            db.query('select * from user_details where user_name = ?', [username], function(err, rows, result) {
                if (err) throw err;
                if(rows.length > 0){
                    res.json({success: "2", message: "username exists"});
                }else {
                    db.query('INSERT INTO user_details VALUES(?,?, ?, ?, ?, ?,?,?,?)', [0,email,username, password,street,city,state,zipcode,phone], function(err, result) {
                        if (err) throw err;
                        res.json({success: "1", userID: 1, message: "registered"});
                        console.log(result.insertId);
                    });
                }
            });
        }
    });
})
module.exports = router;

