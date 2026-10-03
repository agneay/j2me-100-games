#!/usr/bin/env python3
"""Download the pinned build toolchain into tools/lib (Python 3 stdlib only).

Every file is fetched from Maven Central and verified against a SHA-256
pinned below, so builds are reproducible and tamper-evident.

  ecj        Eclipse compiler: compiles Java 1.3 source to preverified CLDC bytecode
  cldc/midp  MicroEmulator API stubs used as the boot classpath (CLDC 1.0 + MIDP 2.0)
  ant        Apache Ant, run straight from the jar
  teavm/*    TeaVM, used only to build the browser demos

usage: python3 tools/bootstrap.py [--no-teavm]
"""
import hashlib
import os
import sys
import urllib.request

REPO = "https://repo1.maven.org/maven2/"
HERE = os.path.dirname(os.path.abspath(__file__))
LIB = os.path.join(HERE, "lib")

CORE = [
    ("org/apache/ant/ant/1.10.15/ant-1.10.15.jar", "763acda4a69588c9ea8817a952851ff0c2fc4bffa1d081c2565dc407f29d5794"),
    ("org/apache/ant/ant-launcher/1.10.15/ant-launcher-1.10.15.jar", "5c8551990307a032336d98ddaed549a39a689f07d4d4c6b950601bf22b3d6a1b"),
    ("org/microemu/cldcapi10/2.0.4/cldcapi10-2.0.4.jar", "6b5753c830a966d91cbfcd5cde223a6899c6c69f021a8e38a429cec967bc290d"),
    ("org/microemu/midpapi20/2.0.4/midpapi20-2.0.4.jar", "b7f6ed5e0344c7000643ecc114e6edff3587e8e77face546af0e12ffd6dbe826"),
    ("org/eclipse/jdt/ecj/3.26.0/ecj-3.26.0.jar", "ac0ba5876eaf7ebb47749a0d1be179c51f194b9dd0b875d1c09e1b530f5a2db5"),
]

T = "org/teavm/"
TEAVM = [
    ("com/carrotsearch/hppc/0.9.1/hppc-0.9.1.jar", "d58706a2be60c972452550cdba79870bf481447c50eb718308e33a6ba45c65ec"),
    ("com/jcraft/jzlib/1.1.3/jzlib-1.1.3.jar", "89b1360f407381bf61fde411019d8cbd009ebb10cff715f3669017a031027560"),
    ("commons-io/commons-io/2.16.1/commons-io-2.16.1.jar", "f41f7baacd716896447ace9758621f62c1c6b0a91d89acee488da26fc477c84f"),
    ("joda-time/joda-time/2.12.2/joda-time-2.12.2.jar", "8d4d807adc8e92a83913d9ccd61e634b4907c78d88f3016d06b3b3d2b4795a02"),
    ("org/mozilla/rhino/1.7.14/rhino-1.7.14.jar", "c9290b0d801bf0dbbbc44338e0f769b7650a0c5d04e6bb1aeb85775c0211b003"),
    ("org/ow2/asm/asm/9.7/asm-9.7.jar", "adf46d5e34940bdf148ecdd26a9ee8eea94496a72034ff7141066b3eea5c4e9d"),
    ("org/ow2/asm/asm-analysis/9.7/asm-analysis-9.7.jar", "7bc6bcbc21379948a0c8c467fb0f864206e5b818f6bc0b546872f5c9f941556f"),
    ("org/ow2/asm/asm-commons/9.7/asm-commons-9.7.jar", "389bc247958e049fc9a0408d398c92c6d370c18035120395d4cba1d9d9304b7a"),
    ("org/ow2/asm/asm-tree/9.7/asm-tree-9.7.jar", "62f4b3bc436045c1acb5c3ba2d8ec556ec3369093d7f5d06c747eb04b56d52b1"),
    ("org/ow2/asm/asm-util/9.7/asm-util-9.7.jar", "37a6414d36641973f1af104937c95d6d921b2ddb4d612c66c5a9f2b13fc14211"),
    (T + "teavm-classlib/0.10.2/teavm-classlib-0.10.2.jar", "e6f2678c621cbbd1292c57d0730240167bd7464c705cc3a2951cbae32cdea5ae"),
    (T + "teavm-core/0.10.2/teavm-core-0.10.2.jar", "93528533ebde9b45a062db5bf9570f547bf369d27ebe6df22259a3fed64eab66"),
    (T + "teavm-interop/0.10.2/teavm-interop-0.10.2.jar", "e44e47ab31a0f47f7d47ff8a07396b2d24f0784bc4756120a54990f3705caacc"),
    (T + "teavm-jso/0.10.2/teavm-jso-0.10.2.jar", "f8182864f4f4a7970bd53f2145480e58bc96d2290e869e44e2ee287864d2e3dc"),
    (T + "teavm-jso-apis/0.10.2/teavm-jso-apis-0.10.2.jar", "d2b2ef1ec999e3da59bc5fc482069dc908d8bbba2a0e8e1c83571a2b24269ad2"),
    (T + "teavm-jso-impl/0.10.2/teavm-jso-impl-0.10.2.jar", "79463191ba476174aa69af4fcd101cd246761a8cf166f593f6c277b33cd3910c"),
    (T + "teavm-metaprogramming-api/0.10.2/teavm-metaprogramming-api-0.10.2.jar", "c877490e8403d80f30c525e9f4c41b27f06f9586860d6e26489f8504592c1859"),
    (T + "teavm-metaprogramming-impl/0.10.2/teavm-metaprogramming-impl-0.10.2.jar", "a74428b5f296d54e6da315ac5bfb438af7cca453aa4b1f3270bee1e27bea19b1"),
    (T + "teavm-platform/0.10.2/teavm-platform-0.10.2.jar", "4098465fe5a9672776d46f97dfa7e8403b884b5096f46113bd225eb6b3025a1d"),
    (T + "teavm-relocated-libs-asm/0.10.2/teavm-relocated-libs-asm-0.10.2.jar", "e5a9a8295664317559c80f795a5a168e707fd5960369ab9724b05d4b6064ff17"),
    (T + "teavm-relocated-libs-asm-analysis/0.10.2/teavm-relocated-libs-asm-analysis-0.10.2.jar", "b96be45813003580805f167f228d0ccfcd1398e3c713b80aba0f7709084c2aab"),
    (T + "teavm-relocated-libs-asm-commons/0.10.2/teavm-relocated-libs-asm-commons-0.10.2.jar", "d6311d9b5a815137641bfe8fec4c636a0bea15635b28b463f3c761ffa5cbd66b"),
    (T + "teavm-relocated-libs-asm-tree/0.10.2/teavm-relocated-libs-asm-tree-0.10.2.jar", "b0276d141ee012a7189012e076d024477fd2cb49f07e63c908c1638ba8bbcc7d"),
    (T + "teavm-relocated-libs-asm-util/0.10.2/teavm-relocated-libs-asm-util-0.10.2.jar", "a703a24424df22b7e5bd2e7c722b2f03f2d448147faf610764ae3521acd726bd"),
    (T + "teavm-relocated-libs-commons-io/0.10.2/teavm-relocated-libs-commons-io-0.10.2.jar", "ec6a63824f4f4a02f14de2ad4213deeeb609ef9032cf510c88ad39d8f740c163"),
    (T + "teavm-relocated-libs-hppc/0.10.2/teavm-relocated-libs-hppc-0.10.2.jar", "2e719b882c3a2eb954401abd69241a7e1826b940e80bcc02b973f10a3639f50a"),
    (T + "teavm-relocated-libs-rhino/0.10.2/teavm-relocated-libs-rhino-0.10.2.jar", "2275220f19175002f1055694099b2a328eb3f14c79e2ec3f2c5502b116b580ad"),
    (T + "teavm-tooling/0.10.2/teavm-tooling-0.10.2.jar", "812e5ad0417919a40642f14a09134d3bd92a6499624b8681f16e63d273b3ac9a"),
]


def sha256(path):
    h = hashlib.sha256()
    with open(path, "rb") as f:
        for chunk in iter(lambda: f.read(65536), b""):
            h.update(chunk)
    return h.hexdigest()


def fetch(rel, digest, dest_dir):
    os.makedirs(dest_dir, exist_ok=True)
    dest = os.path.join(dest_dir, rel.rsplit("/", 1)[1])
    if os.path.exists(dest) and sha256(dest) == digest:
        return False
    tmp = dest + ".part"
    with urllib.request.urlopen(REPO + rel, timeout=120) as r, open(tmp, "wb") as f:
        f.write(r.read())
    got = sha256(tmp)
    if got != digest:
        os.remove(tmp)
        raise SystemExit("checksum mismatch for %s: %s" % (rel, got))
    os.replace(tmp, dest)
    return True


def main():
    n = 0
    for rel, digest in CORE:
        n += fetch(rel, digest, LIB)
    if "--no-teavm" not in sys.argv:
        for rel, digest in TEAVM:
            n += fetch(rel, digest, os.path.join(LIB, "teavm"))
    print("toolchain ready in %s (%d downloaded)" % (LIB, n))


if __name__ == "__main__":
    main()
