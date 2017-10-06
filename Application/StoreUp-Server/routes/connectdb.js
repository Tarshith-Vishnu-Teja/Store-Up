var mysql = require('mysql'),
    db = null;
module.exports = function () {
    if(!db) {
        db = mysql.createConnection({
            host:       "localhost",
            user:       "root",
            password:   "1234",
            database:   "storeup"
        });
    };
    return db;
};
