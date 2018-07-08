
theta-plugin-sdkの公式リポジトリのコードに手を入れて
sora-android-sdkで動かすようにしたもの

## 状態

- まだ不安定。動画を送信しているといきなりクラッシュすることがある。
- マイクの処理がうまくいってなくて、音のupstreamを設定すると雑音が送信される。

## Getting Started

### Signaling設定

MainActivityの該当箇所に

- シグナリングエンドポイント
- チャンネルID
- メタデータ

をベタ書きするととりあえずは実験できる

ちゃんとやるならConfigサーバーまわりを仕上げる

### メディアデータ

以下のLive640をLive1024, Live1920, Live3840などにすると
縦横のサイズが変わる。

```
new ThetaCameraCapturer(ThetaCameraCapturer.ShootingMode.Live640);
```

- Live3840: 3840x1920
- Live1920: 1920x960
- Live1024: 1024x512
- Live640:  640x320


### インストール

THETA V自体は以下の準備ができている前提とする

- 最新のファームウェア、
- 開発者モード
- Wifiクライアントモード

以下の作業を行い、このリポジトリのアプリケーションを突っ込み、選択

1. Build -> APKのビルド
2. 同じpackage名のプラグインが既にthetaに入っていたら削除 `adb uninstall com.theta360.pluginapplication`
3. ビルドしたAPKをインストール `adb install ./theta-plugin-sdk/app/build/outputs/apk/debug/app-debug.apk`
4. Vysorで設定アプリ -> App -> Plugin Application(このアプリ) -> Permissionのところを開き全部チェック(http://theta360.guide/plugin-guide/tutorialvysor/)
5. THETA Vを一度offにしてonにしなおす
6. THETAのiOS/AndroidアプリケーションでTHETAに接続し、このプラグインを選択する
7. modeボタン長押しでプラグインモードに以降

## LEDなどデバイスの状態について

1. このプラグインが選択されている状態で、プラグイン起動する(プラグインモードに以降する)。ビデオとLIVEのLEDが青で光る
2. 撮影ボタンを押すと、LIVE LEDの下が赤く光り、撮影中であることが分かる。撮影開始の音も鳴る。
3.  ネットワークのLEDが黄色くなる(接続中)。その後接続完了するとシアンになる。
4. もう一度撮影ボタンを押すと、撮影終了の音が鳴り接続が閉じる。撮影中を表す赤いライトは消え, ネットワークLEDは青くなる。

