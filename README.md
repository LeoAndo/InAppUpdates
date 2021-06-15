# InAppUpdates
sample for InAppUpdates

### Overview
アプリ内アップデートは、Android 5.0（APIレベル21）以降を実行しているデバイスでのみ機能し<br>
Play Core library 1.5.0以降を使用する必要があります。<br>
これらの要件を満たした後、アプリはアプリ内アップデート用に次のUXをサポートできます。<br>

### Immediate

強制アップデートで、全画面表示される。<br>
ユーザはアプリ更新を拒否できない。アップデートしないとアプリが使えない.<br>

<img src="https://user-images.githubusercontent.com/16476224/62906028-f63ea700-bda7-11e9-866f-d3cf437496e6.png" width=320>

### Flexible

任意アップデートで、ユーザーはアプリ更新を拒否できる。<br>
アップデート処理がバックグランドで行われるため、アプリを引き続き操作できる.<br>

<img src="https://user-images.githubusercontent.com/16476224/62906042-0787b380-bda8-11e9-8531-848d10fcd26a.png" width=320>

### テスト時のメモ

参考:
https://developer.android.com/guide/playcore/in-app-updates/test

テスト方法
1. Google PlayにAPKをリリースした開発者アカウントでログインする
2. 最低１回は、Google PlayからAPKをインストールする
3. Android Studioからバージョンコードを下げて、Android端末にインストールする
    ※この時、ダウングレードになるので、アプリアンインストールしてインストールという形になる.
4. アプリを起動して操作する.

注：アプリをAndroidアプリバンドルとして公開する場合、アプリ内更新を使用するアプリの圧縮ダウンロードの最大許容サイズは150MBです。<br>
アプリ内更新は、APK拡張ファイル（.obbファイル）を使用するアプリと互換性がありません。<br>

### ドキュメント
https://developer.android.com/guide/app-bundle/in-app-updates

### API
https://developer.android.com/reference/com/google/android/play/core/classes
