<?php
$posts = array('user', 'pass', 'orders');
$gets = array('action', 'latitude', 'longtitude', 'time');
if (isset($_POST[$posts[0]]) && isset($_POST[$posts[1]]) && isset($_GET[$gets[0]])) {
	include("connect.php");
	include("../demo/technology/wp-includes/class-phpass.php");
	
	// FIRST
	$actions = array('login', 'whereIAm', 'filterOrders');
	$ourTable = "wc_delivery_driver_details";
	$unknownError = "an unknown error occurred";
	$wrongUser = "wrong username or password";
	$wrongPass = "wrong password";//username or 
	$incomInfo = "incomplete information";
	
	// LOGIN
	$login = $c->query("SELECT ID, user_pass FROM wp_users WHERE user_login='".$_POST[$posts[0]]."'");
	if (!$login) dai($unknownError); 
	$id;$pass;
	if ($login->num_rows <= 0) dai($wrongUser);
	else {
		while($row = $login->fetch_assoc()) {
			$id = $row['ID'];
			$pass = $row['user_pass'];
		}
		$wp_hasher = new PasswordHash(8, TRUE);
		if($wp_hasher->CheckPassword($_POST[$posts[1]], $pass)) {
			// CONTINUES AT THE BOTTOM...
		} else dai($wrongPass);
	}
	
	// CONFIRM that he's a Delivery-Man
	$confirm = $c->query("SELECT meta_value FROM wp_usermeta WHERE user_id='$id' AND meta_key='wp_capabilities'");
	if (!$confirm) dai($unknownError);
	$delman = false;
	if ($confirm->num_rows <= 0) dai($notFound);
	else while($row = $confirm->fetch_assoc()) if (strstr($row['meta_value'], "driver")) $delman = true;
	if ($delman) {
		if ($_GET[$gets[0]] == $actions[0]) dai("loggedIn");
	} else dai($wrongPass);
	
	// CHECK FOR TABLE
	$tCheck = $c->query("SHOW TABLES LIKE '".$ourTable."'");
	if ($tCheck->num_rows != 1) {
		$sql = "CREATE TABLE $ourTable (
id INT(10) PRIMARY KEY,
latitude VARCHAR(20) NOT NULL,
longtitude VARCHAR(20) NOT NULL,
time VARCHAR(50) NOT NULL
)";
		if ($c->query($sql) !== TRUE) dai("could not create a proper table in the database");
	}
	
	// OTHER ACTIONS
	switch ($_GET[$gets[0]]) {
		case $actions[1]:// WHERE I AM...
		    if (!isset($_GET[$gets[1]]) || !isset($_GET[$gets[2]]) || !isset($_GET[$gets[3]])) dai($incomInfo);
			$isInTable = $c->query("SELECT * FROM $ourTable WHERE id='".$id."'");
			if ($isInTable->num_rows > 0) {
				$sql = "UPDATE $ourTable SET latitude='".$_GET[$gets[1]]."', longtitude='".$_GET[$gets[2]]."', time='".$_GET[$gets[3]]."' 
WHERE id='".$id."'";
				if ($c->query($sql) === TRUE) echo "ok";
				else dai("could not update");
			} else {
				$sql = "INSERT INTO $ourTable (id, latitude, longtitude, time) 
VALUES ('".$id."', '".$_GET[$gets[1]]."', '".$_GET[$gets[2]]."', '".$_GET[$gets[3]]."')";
				if ($c->query($sql) === TRUE) echo "ok";
				else dai("could not update");
			}
			break;
			
		case $actions[2]:// FILTER ORDERS
			if (!isset($_POST[$posts[2]])) dai($incomInfo);
			$split = explode(";", $_POST[$posts[2]]);
			$echo = "aaaaa";
			for ($i = 0; $i < count($split); $i++) {//is_numeric()
				$who = $c->query("SELECT meta_value FROM wp_postmeta WHERE post_id='".$split[$i]."' AND meta_key='ddwc_driver_id'");
				if ($who) if ($who->num_rows > 0) while($row = $who->fetch_assoc()) if ($row['meta_value'] == "$id") {
					$echo .= $split[$i];
					if ($i != count($split) - 1) $echo .= ";";
				}
			}
			echo $echo;
		    break;
	}
	
	$c->close();
} else echo "404 not found"; ?>