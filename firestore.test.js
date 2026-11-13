const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read or list projects", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("projects").get()
  );
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("projects")
      .doc("proj_1")
      .get()
  );
});

test("Authenticated user: can create, read, list, update, and delete their own project", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("projects")
    .doc("proj_1");

  const now = new Date(Date.now() - 1000);
  await assertSucceeds(
    docRef.set({
      userId: ALICE_UID,
      title: "Neon Velocity Cut",
      aspectRatioName: "RATIO_9_16",
      resolutionName: "RES_1080P",
      fpsName: "FPS_30",
      durationMs: 12000,
      clipCount: 3,
      payloadJson: '{"videoClips":[]}',
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(docRef.get());
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("projects").get()
  );

  await assertSucceeds(
    docRef.update({
      title: "Neon Velocity Master",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  await assertSucceeds(docRef.delete());
});

test("Authenticated user: cannot read, list, or modify another user's projects", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const now = new Date(Date.now() - 2000);
    await context
      .firestore()
      .collection("users")
      .doc(BOB_UID)
      .collection("projects")
      .doc("bob_proj")
      .set({
        userId: BOB_UID,
        title: "Bob Secret Edit",
        aspectRatioName: "RATIO_16_9",
        resolutionName: "RES_1080P",
        fpsName: "FPS_60",
        durationMs: 8000,
        clipCount: 2,
        payloadJson: '{"videoClips":[]}',
        createdAt: now,
        updatedAt: now,
      });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb
      .collection("users")
      .doc(BOB_UID)
      .collection("projects")
      .doc("bob_proj")
      .get()
  );
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("projects").get()
  );
  await assertFails(
    aliceDb
      .collection("users")
      .doc(BOB_UID)
      .collection("projects")
      .doc("bob_proj")
      .delete()
  );
});

test("Shadow update & value poisoning: rejects extra keys or invalid types", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("projects")
    .doc("proj_shadow");
  const now = new Date(Date.now() - 1000);

  await assertFails(
    docRef.set({
      userId: ALICE_UID,
      title: "Shadow Project",
      aspectRatioName: "RATIO_9_16",
      resolutionName: "RES_1080P",
      fpsName: "FPS_30",
      durationMs: 5000,
      clipCount: 1,
      payloadJson: '{"videoClips":[]}',
      isAdmin: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertFails(
    docRef.set({
      userId: BOB_UID,
      title: "Spoofed UID Project",
      aspectRatioName: "RATIO_9_16",
      resolutionName: "RES_1080P",
      fpsName: "FPS_30",
      durationMs: 5000,
      clipCount: 1,
      payloadJson: '{"videoClips":[]}',
      createdAt: now,
      updatedAt: now,
    })
  );
});
