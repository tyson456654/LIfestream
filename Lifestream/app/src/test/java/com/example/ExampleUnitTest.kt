package com.example

import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.util.Base64

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun encodeApkToTsx() {
    // Let's locate the apk file. It could be at the root or module level.
    var apkFile = File("../app-debug.apk")
    if (!apkFile.exists()) {
      apkFile = File("app-debug.apk")
    }
    if (!apkFile.exists()) {
      apkFile = File("../../app-debug.apk")
    }
    if (!apkFile.exists()) {
      // Look in outputs
      apkFile = File("build/outputs/apk/debug/app-debug.apk")
    }
    if (!apkFile.exists()) {
      apkFile = File("../app/build/outputs/apk/debug/app-debug.apk")
    }

    if (!apkFile.exists()) {
      println("Skipping APK encoding because app-debug.apk has not been generated yet.")
      return
    }

    val bytes = apkFile.readBytes()
    val base64String = Base64.getEncoder().encodeToString(bytes)

    val tsxContent = """
// apk.tsx
// This file contains the base64-encoded APK of the LifeStream application.
// You can copy the base64 string below or download this file and decode it.

export const apkBase64 = "$base64String";

export default function ApkDownloadButton() {
  const handleDownload = () => {
    const link = document.createElement('a');
    link.href = 'data:application/vnd.android.package-archive;base64,' + apkBase64;
    link.download = 'LifeStream.apk';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <button 
      onClick={handleDownload}
      style={{
        padding: '12px 24px',
        backgroundColor: '#D32F2F',
        color: 'white',
        border: 'none',
        borderRadius: '8px',
        cursor: 'pointer',
        fontSize: '16px',
        fontWeight: 'bold',
        boxShadow: '0 4px 6px rgba(0,0,0,0.1)',
        transition: 'background-color 0.2s'
      }}
    >
      Download LifeStream APK
    </button>
  );
}
""".trimIndent()

    // Determine destination at the root
    var destFile = File("../apk.tsx")
    val parent = destFile.parentFile
    if (parent == null || !parent.exists() || parent.name != "applet") {
      // Fallback
      destFile = File("apk.tsx")
    }
    
    // Also try writing to the root directory
    val rootDir = File(".").absoluteFile.parentFile
    if (rootDir != null) {
      val rootApkTsx = File(rootDir, "apk.tsx")
      rootApkTsx.writeText(tsxContent)
      println("Successfully wrote apk.tsx to: ${rootApkTsx.absolutePath}")
    }
    
    // Also write to current dir
    destFile.writeText(tsxContent)
    
    println("Successfully wrote apk.tsx to: ${destFile.absolutePath}")
  }
}

