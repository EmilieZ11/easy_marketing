<?php
$gets = array('action');
$posts = array('user', 'set_driver', 'new_driver');
if (!isset($_GET[$gets[0]]) || !isset($_POST[$posts[0]])) die("404 not found");
include("connect.php");

// FIRST
$notFound = "not found";
$unknownError = "an unknown error occurred";
$noAction = "no action specified";
$couldNotUpdate = "could not update";
$actions = array('login', 'drivers', 'set_driver', 'remove_driver');

// LOGIN and get his ID
$login = $c->query("SELECT ID FROM wp_users WHERE user_login='".$_POST[$posts[0]]."' LIMIT 1");
if (!$login) dai($unknownError); 
$id;
if ($login->num_rows <= 0) dai($notFound);
else while($row = $login->fetch_assoc()) $id = $row['ID'];

// CONFIRM that he's a Shop Manager
$confirm = $c->query("SELECT meta_value FROM wp_usermeta WHERE user_id='$id' AND meta_key='wp_capabilities'");
if (!$confirm) dai($unknownError);
$loggedIn = false;
if ($confirm->num_rows <= 0) dai($notFound);
else while($row = $confirm->fetch_assoc()) if (strstr($row['meta_value'], "shop_manager")) $loggedIn = true;
if ($loggedIn) {
	if ($_GET[$gets[0]] == $actions[0]) dai("notFound");
	// CONTINUED AT THE BOTTOM...
} else dai($notFound);


switch ($_GET[$gets[0]]) {
    case $actions[1]:// VIEW ALL DRIVERS
        include("json_drivers.php");
		$json = allDrivers($c);
		echo $json;
        break;
	case $actions[2]:// ASSIGN A DRIVER FOR AN ORDER
	    $checkExists = $c->query("SELECT meta_value FROM wp_postmeta WHERE post_id='".$_POST[$posts[1]]."' AND meta_key='ddwc_driver_id'");
		$sql = "INSERT INTO wp_postmeta (post_id, meta_key, meta_value) VALUES ('".$_POST[$posts[1]]."', 'ddwc_driver_id', '".$_POST[$posts[2]]."')";
		//$newStatus = "wc-driver-assigned";
		$shallIExec = true;
		if ($checkExists) {
			if ($checkExists->num_rows > 0) {
				if ($_POST[$posts[2]] == "-1") {
					$sql = "DELETE FROM wp_postmeta WHERE post_id='".$_POST[$posts[1]]."' AND meta_key='ddwc_driver_id'";
					//$newStatus = "wc-processing";
				} else $sql = "UPDATE wp_postmeta SET meta_value='".$_POST[$posts[2]]."' WHERE post_id='".$_POST[$posts[1]]."' AND meta_key='ddwc_driver_id'";
			} else if ($_POST[$posts[2]] == "-1") $shallIExec = false;
		} else if ($_POST[$posts[2]] == "-1") $shallIExec = false;
		
		if ($shallIExec) {
			if ($c->query($sql) === TRUE) {
				//$c->query("UPDATE wp_wc_order_stats SET status='".$newStatus."' WHERE order_id='".$_POST[$posts[1]]."'");
				echo "done";
			} else dai($couldNotUpdate);
		}
		break;
    default:// Doesn't need "break;"
		dai($noAction);
}


$c->close();