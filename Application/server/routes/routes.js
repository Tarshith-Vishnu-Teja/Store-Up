var router     = require('express').Router(),
    db         = require('./connectdb')(),
    formidable = require('formidable'),
    fs         = require('fs-extra'),
    util       = require('util'),
    path       = require('path');

db.connect(function(err) {
    if (err) { console.error(err.stack); return;}
});

router.use(function(req, res, next) {
    console.log('--new request--');
    next(); // visit next routes
});


// LOGIN Route
router.route('/login').post(function(req, res) {
    var tag      = req.body.tag,
        email    = req.body.email,
        password = req.body.password;

    req.checkBody('email', 'not a valid email.').isEmail();
    req.checkBody('password', '8-32 character').len(8, 32);
    var errors = req.validationErrors();
    console.log(email);
    console.log(password);
    console.log(tag);
    /*if(email=="krishna@gmail.com" && password=="krishnark1993")
        res.json({success: "1", userid: "1", message: "user logged in"});
    else
        res.json({success: "0", message: "invalid email or password"});*/

    db.query('select * from user_details where email = ? and password = ?', [email, password], function(err, rows, fields) {
        if (err) throw err;

        if (rows.length > 0) {
            res.json({success: "1", userid: 1, message: "user logged in"});
        }else {
            res.json({success: "0", message: "invalid email or password"});
        }
    })
});

// REGISTER Route
router.route('/register').post(function(req, res) {
    var tag      = req.body.tag,
        username = req.body.username,
        email    = req.body.email,
        password = req.body.password,
        street = req.body.street,
        city = req.body.city,
        state = req.body.state,
        zipcode = req.body.zipcode,
        phone = req.body.phone;

    req.checkBody('email', 'not a valid email.').isEmail();
    req.checkBody('username', '2-32 character').len(2, 32);
    req.checkBody('password', '8-32 character').len(8, 32);
    var errors = req.validationErrors();

    if (tag === "register") {
        if (errors) {
            res.json({message: errors});
        }else {
        //check whether user exists
        db.query('select * from users where email = ?', [email], function(err, rows, result) {
            if (err) throw err;
            if(rows.length > 0){
                res.json({success: "0", message: "email exists"});
            }else {
                //check username exists
                db.query('select * from users where username = ?', [username], function(err, rows, result) {
                    if (err) throw err;
                    if(rows.length > 0){
                        res.json({success: "2", message: "username exists"});
                    }else {
                        db.query('INSERT INTO users VALUES(null, ?, ?, ?)', [username, email, password], function(err, result) {
                          if (err) throw err;
                          res.json({success: "1", userID: result.insertId, message: "registered"});
                          console.log(result.insertId);
                        });
                    }
                });
            }
        });
        }

    }else {
        res.json({success: "2", message: "not a register request"});
    }

});

module.exports.router = router;
