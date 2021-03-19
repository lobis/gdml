void Gdml(string gdmlFilename="Setup.gdml"){
    TGeoManager::Import("Setup.gdml");
    TGeoElementTable *table = gGeoManager->GetElementTable();
    float transparency = 40;
    // this probably does not work too well with nested volumes
    for (int i = 0; i < gGeoManager->GetListOfVolumes()->GetEntries(); i++) {
        TGeoVolume* geoVolume = gGeoManager->GetVolume(i); // https://root.cern/doc/v606/classTGeoVolume.html
        if (geoVolume == nullptr){
            continue;
        }
        string materialName = geoVolume->GetMaterial()->GetName();
        if (materialName == "G4_AIR" || materialName == "G4_Galactic" || materialName == "Vacuum"){
             geoVolume->SetTransparency(100);
        }else{
             geoVolume->SetTransparency(transparency);
        }
        // geoVolume->Raytrace();
        //geoVolume->SetTransparency(transparency);
        // geoVolume->SetLineColor(kBlue);

        geoVolume->Print();
        cout << endl << endl;;
        //geoVolume->CheckOverlaps();
        //geoVolume->CheckShapes();
        //geoVolume->CheckGeometry();
        //cout << "volume: " << i << " density=" << density << endl;
    }

    TEveManager::Create();
    TGeoNode* node = gGeoManager->GetTopNode();
    node->CheckOverlaps(0.0001);
    TEveGeoTopNode* top_node = new TEveGeoTopNode(gGeoManager, node);
    gEve->AddGlobalElement(top_node);

    gEve->FullRedraw3D(kTRUE);
    auto viewer = gEve->GetDefaultGLViewer();
    // viewer->GetClipSet()->SetClipType(TGLClip::EType(2));
    viewer->CurrentCamera().Reset();
    viewer->SetCurrentCamera(TGLViewer::kCameraOrthoZOY); // side view
    //viewer->SetCurrentCamera(TGLViewer::kCameraOrthoXOY); // front view
    //viewer->SetCurrentCamera(TGLViewer::kCameraOrthoXOZ); // top view
    //viewer->SetCurrentCamera(TGLViewer::kCameraPerspXOZ);
    //viewer->CurrentCamera().RotateRad(0, -0.25);
    
    viewer->DoDraw();
}