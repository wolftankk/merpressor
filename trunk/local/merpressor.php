<?php

error_reporting(0);

// get parameters
$home = $_GET['home'];
$type = $_GET['type'];
$config = $home.$_GET['config'].'.'.$type.'.xml';

$mimeType = 'application/x-javascript';
$charset = 'utf-8';

$output = array();
$protocal = (isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] != 'off') ? 'https://' : 'http://';
$prefix = $protocal.$_SERVER['SERVER_NAME'].$home;

$doc = new DOMDocument();
$doc->load($_SERVER['DOCUMENT_ROOT'].$config);

// get encoding
$list = $doc->getElementsByTagName('list');
if($list->length > 0){
	$charset = $list->item(0)->getAttribute('encoding');
	if($charset == ''){
		$charset = 'utf-8';
	}
}

// get file list
$files = $doc->getElementsByTagName('file');
$i = 0;
if(strcasecmp($type, 'css') == 0){
	$mimeType = 'text/css';
	foreach($files as $value){
		$output[$i++] = '@import url("'.$prefix.$value->nodeValue.'");';
	}
}
else{
	foreach($files as $value){
		$output[$i++] = 'document.write(\'<script type="text/javascript" src="'.$prefix.$value->nodeValue.'"></\' + \'script>\');';
	}
}

// do output
header('Content-Type: '.$mimeType.'; charset='.$charset);
echo join("\n", $output);

?>