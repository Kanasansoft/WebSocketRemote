= WebSocketRemote

WebsocketRemoteは、VNCに似たリモート制御アプリケーションです。
クライアント側では、WebSocketに対応したウェブブラウザだけが必要で、プラグインやアプレットは不要です。

== ダウンロード

http://github.com/Kanasansoft/WebSocketRemote/downloads

== 実行

サーバ側で、WebSocketRemoteの実行ファイルをダブルクリックするか、ターミナルからコマンドを実行して下さい。

実行ファイル(x.x.xはバージョン番号です。)
* Windows用:WebSocketRemote-x.x.x.exe.
* Macintosh用:WebSocketRemote-x.x.x.app.

コマンド(x.x.xはバージョン番号です。)

% java -jar WebSocketRemote-x.x.x.jar 

WebSocketRemoteのアイコンが表示されていれば、実行中となります。

* Windows用:タスクトレイに表示
* Macintosh用:メニューバーに表示
* Linux用:メニューバーに表示

== 使い方

クライアント側からウェブブラウザで"http:[server address]:40320/"にアクセスして下さい。

== 終了

WebSocketRemoteのiconを右クリックします。
Quitを選択して下さい。

== 必須条件

=== サーバ側

Java SE 6

=== クライアント側

WebSocketに対応したウェブブラウザ

== 注釈

現バージョンはマウスイベントのみサポートしています。
現バージョンはポート番号の変更はできません。
