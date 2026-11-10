import 'package:flutter/material.dart';
import 'package:flutter_inappwebview/flutter_inappwebview.dart';
import 'package:url_launcher/url_launcher.dart';

void main() => runApp(const MyApp());

class MyApp extends StatelessWidget {
  const MyApp({super.key});
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      home: const WebViewScreen(),
    );
  }
}

class WebViewScreen extends StatefulWidget {
  const WebViewScreen({super.key});
  @override
  State<WebViewScreen> createState() => _WebViewScreenState();
}

class _WebViewScreenState extends State<WebViewScreen> {
  InAppWebViewController? webViewController;
  bool isLoading = true;

  @override
  Widget build(BuildContext context) {
    return PopScope(
      canPop: false,
      onPopInvoked: (didPop) async {
        if (didPop) return;
        if (await webViewController?.canGoBack() ?? false) {
          webViewController?.goBack();
        }
      },
      child: Scaffold(
        body: SafeArea(
          child: Stack(
            children: [
              InAppWebView(
                initialUrlRequest: URLRequest(url: WebUri('https://bilgilerburda.com/faucet')),
                initialSettings: InAppWebViewSettings(
                  javaScriptEnabled: true,
                  mediaPlaybackRequiresUserGesture: false,
                  useShouldOverrideUrlLoading: true,
                  domStorageEnabled: true,
                  databaseEnabled: true,
                  transparentBackground: true,
                ),
                onWebViewCreated: (controller) {
                  webViewController = controller;
                },
                onLoadStart: (controller, url) {
                  setState(() => isLoading = true);
                },
                onLoadStop: (controller, url) async {
                  setState(() => isLoading = false);
                  // JavaScript zamanlayıcılarını (timer) yeniden başlat
                  if (controller.platform is AndroidInAppWebViewController) {
                    await (controller.platform as AndroidInAppWebViewController).resumeTimers();
                  }
                },
                shouldOverrideUrlLoading: (controller, navigationAction) async {
                  var uri = navigationAction.request.url;
                  if (uri != null && !uri.toString().startsWith('https://bilgilerburda.com')) {
                    launchUrl(uri, mode: LaunchMode.externalApplication);
                    return NavigationActionPolicy.CANCEL;
                  }
                  return NavigationActionPolicy.ALLOW;
                },
              ),
              if (isLoading)
                const Center(child: CircularProgressIndicator()),
            ],
          ),
        ),
      ),
    );
  }
}
