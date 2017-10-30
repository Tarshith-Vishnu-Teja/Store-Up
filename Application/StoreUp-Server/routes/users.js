var express = require('express');
var router = express.Router();
var db = require('./connectdb')();

/* GET users listing. */
router.get('/userDetails', function(req, res, next) {
    console.log("Comes here");
    var email = req.body.email;

    console.log(email);

    db.query('select * from user_details where email = ?', [email], function(err, rows, fields) {
        if (err) throw err;

        if (rows.length > 0) {
            res.json({success: "1", username: rows[0], message: "user details retrieved"});
        }else {
            res.json({success: "0", message: "error retrieving user details"});
        }
    })

});

module.exports = router;
