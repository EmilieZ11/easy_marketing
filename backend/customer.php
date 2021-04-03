<?php
$posts = array('findDelivery', 'call');
if (isset($_POST[$posts[0]]) || isset($_POST[$posts[1]])) {
	include("connect.php");
	
	// FIRST
	$ourTable = "wc_delivery_driver_details";
	$tUserMeta = "wp_usermeta";
	$anError = "an error ocurred";
	$notFound = "not found";
	$notAssigned = "notAssigned";
	
	
	// GET THE DELIVERY-MAN'S ID
	$orderId;$id;
	if (isset($_POST[$posts[0]])) $orderId = $_POST[$posts[0]];
	if (isset($_POST[$posts[1]])) $orderId = $_POST[$posts[1]];
	$who = $c->query("SELECT meta_value FROM wp_postmeta WHERE post_id='$orderId' AND meta_key='ddwc_driver_id'");
	//Don't forget to always add tiny quotion mark at the sides of the parameters.
	if (!$who) dai($anError); 
	if ($who->num_rows > 0) {
		$id = "";
		while($row = $who->fetch_assoc()) $id = $row['meta_value'];
	} else dai($notAssigned);
	
	if (isset($_POST[$posts[0]])) {// FIND THE DELIVERY-MAN
		$tCheck = $c->query("SHOW TABLES LIKE '$ourTable'");
		//It doesn't make any difference to put $ourTable between quotion marks or not!
		if ($tCheck->num_rows != 1) dai($notFound);
		
		$detalis = $c->query("SELECT latitude, longtitude, time FROM $ourTable WHERE id='$id'");
		if (!$detalis) dai($anError); 
		$latitude;$longtitude;$time;
		if ($detalis->num_rows > 0) {
			while($row = $detalis->fetch_assoc()) {
				$latitude = $row['latitude'];
				$longtitude = $row['longtitude'];
				$time = $row['time'];
			}
			echo "aaaaa".$latitude.":".$longtitude.":".$time;
		} else dai($notFound);
		
		
	} else if (isset($_POST[$posts[1]])) {// GET THE DELIVERY-MAN'S PHONE NUMBER
		$uCheck = $c->query("SHOW TABLES LIKE '$tUserMeta'");
		if ($uCheck->num_rows != 1) dai($notFound);
		
		$meta = $c->query("SELECT meta_value FROM $tUserMeta WHERE user_id='$id' AND meta_key='billing_phone'");
		if (!$meta) dai($anError);
		if ($meta->num_rows > 0) {
			$call;
			while($row = $meta->fetch_assoc()) $call = $row['meta_value'];
			echo "aaaaa".$call;
		} else dai($notFound);
	}
		
	$c->close();
} else echo "404 not found"; ?>