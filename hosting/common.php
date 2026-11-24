<?php
declare(strict_types=1);
function normalize(string $s): ?string {
 if (!preg_match('/^[+0-9\s()-]{7,25}$/D',$s)) return null;
 $n=preg_replace('/[^0-9]/','',$s);
 if(str_starts_with($n,'0092')) $n='0'.substr($n,4); elseif(str_starts_with($n,'92')) $n='0'.substr($n,2);
 return preg_match('/^03[0-9]{9}$/D',$n)?$n:null;
}
function url_ok(string $s): bool {return strlen($s)<=255 && filter_var($s,FILTER_VALIDATE_URL)!==false && parse_url($s,PHP_URL_SCHEME)==='https' && !parse_url($s,PHP_URL_USER);}
function e(string $s): string {return htmlspecialchars($s,ENT_QUOTES|ENT_SUBSTITUTE,'UTF-8');}
function setup(): array {
 if (!is_file(__DIR__.'/config.php')) {http_response_code(503);exit('Configuration required');}
 $c=require __DIR__.'/config.php';
 if(!preg_match('/^[a-f0-9]{64}$/D',$c['secret']) || !str_starts_with($c['admin_hash'],'$')) {http_response_code(503);exit('Configuration required');}
 $db=new PDO($c['dsn'],$c['user'],$c['password'],[PDO::ATTR_ERRMODE=>PDO::ERRMODE_EXCEPTION,PDO::ATTR_EMULATE_PREPARES=>false]);return [$db,$c];
}
function rate(PDO $db,array $c,string $scope,int $max): bool {
 $window=intdiv(time(),60);$key=hash_hmac('sha256',$scope.'|'.($_SERVER['REMOTE_ADDR']??'unknown').'|'.$window,$c['secret']);
 $q=$db->prepare('INSERT INTO limits(bucket,hits,expires_at) VALUES (?,1,?) ON DUPLICATE KEY UPDATE hits=hits+1');$q->execute([$key,($window+2)*60]);
 $q=$db->prepare('SELECT hits FROM limits WHERE bucket=?');$q->execute([$key]);$hits=(int)$q->fetchColumn();
 $db->prepare('DELETE FROM limits WHERE expires_at < ? LIMIT 100')->execute([time()]);return $hits<=$max;
}
