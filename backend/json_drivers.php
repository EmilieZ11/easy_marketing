<?php
function allDrivers($c) {
	$drvget = $c->query("SELECT user_id, meta_value FROM wp_usermeta WHERE meta_key='wp_capabilities'");
	if (!$drvget) dai($unknownError);
	$echo = '[';
	if ($drvget->num_rows > 0) while($row = $drvget->fetch_assoc()) if (strstr($row['meta_value'], "driver")) {
		$id = $row['user_id'];
		$echo .= '{"id": '.$id.', ';
		$drvprop = $c->query("SELECT * FROM wp_users WHERE ID='$id' LIMIT 1");
		if (!$drvprop) dai($unknownError);
		if ($drvprop->num_rows == 1) while($raow = $drvprop->fetch_assoc()) {
			$echo .= '"user": "'.$raow["user_login"].'", ';
			$echo .= '"name": "'.$raow["display_name"].'", ';
		} else dai($unknownError);
		
		$drvords = $c->query("SELECT post_id FROM wp_postmeta WHERE meta_key='ddwc_driver_id' AND meta_value='$id'");
		if (!$drvords) dai($unknownError);
		$echo .= '"ords": [';
		if ($drvords->num_rows > 0) {
			while($ruaw = $drvords->fetch_assoc()) $echo .= $ruaw["post_id"].', ';
			$echo = substr($echo, 0, strlen($echo)-2);
		}
		$echo .= ']';
		$echo .= '}, ';
	}
	$echo = substr($echo, 0, strlen($echo)-2);
	$echo .= ']';
	return $echo;
}
?>