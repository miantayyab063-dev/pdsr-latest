<?php
declare(strict_types=1);
require __DIR__.'/common.php';
header('Content-Type: application/json; charset=utf-8');header('Cache-Control: no-store');
function reply(int $code,array $data): never {http_response_code($code);echo json_encode($data,JSON_UNESCAPED_UNICODE|JSON_INVALID_UTF8_SUBSTITUTE);exit;}
if($_SERVER['REQUEST_METHOD']!=='POST')reply(405,['error'=>'POST required']);
if(empty($_SERVER['HTTPS'])||$_SERVER['HTTPS']==='off')reply(403,['error'=>'HTTPS required']);
try {[$db,$c]=setup();if(!rate($db,$c,'search',10))reply(429,['error'=>'Too many requests. Try again later.']);
 $n=normalize(is_string($_POST['number']??null)?$_POST['number']:'');if(!$n)reply(400,['error'=>'Enter a complete Pakistani mobile number']);
 $q=$db->prepare('SELECT phone,business_name,category,website FROM listings WHERE phone=?');$q->execute([$n]);$row=$q->fetch(PDO::FETCH_ASSOC);reply(200,['record'=>$row?:null]);
} catch(Throwable $x){reply(503,['error'=>'Service unavailable']);}
