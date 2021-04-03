<?php
$dbname = "hussein_easyretail";
$host = "mysql1006.mochahost.com";
$user = "hussein_demouser";
$pass = "?rA6xXT(T+VZ";

$c = mysqli_connect($host, $user, $pass, $dbname);
$c->set_charset("utf8");
if (!$c) die("failed to connect");
function dai($mess) {/*if ($c != null) {$c->close();}*/die($mess);}
?>