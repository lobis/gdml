void Gdml(string gdmlFilename="Setup.gdml"){
    // first import it and see if there is something wrong with the file
    TGeoManager::Import(gdmlFilename.c_str());
    // check for overlaps
    TGeoNode* node = gGeoManager->GetTopNode();
    node->CheckOverlaps(0.0001);

    // set transparency
    float transparency = 40;
    for (int i = 0; i < gGeoManager->GetListOfVolumes()->GetEntries(); i++) {
            TGeoVolume* geoVolume = gGeoManager->GetVolume(i); // https://root.cern/doc/v606/classTGeoVolume.html
            if (geoVolume == nullptr){
                continue;
            }
            double density = geoVolume->GetMaterial()->GetDensity();
            string materialName = geoVolume->GetMaterial()->GetName();
            //cout << geoVolume->GetName() << endl;
            if (materialName == "G4_AIR" || materialName == "G4_Galactic" || materialName == "Vacuum"){
                 geoVolume->SetTransparency(100);
            }else{
                 geoVolume->SetTransparency(transparency);
            }

    }
    TEveManager::Create();

    TEveGeoTopNode* top_node = new TEveGeoTopNode(gGeoManager, node);
    gEve->AddGlobalElement(top_node);

    gEve->FullRedraw3D(kTRUE);
    auto viewer = gEve->GetDefaultGLViewer();
    // viewer->GetClipSet()->SetClipType(TGLClip::EType(2));
    viewer->CurrentCamera().Reset();
    viewer->SetCurrentCamera(TGLViewer::kCameraPerspXOZ);
    //viewer->CurrentCamera().RotateRad(0, -0.25);
    /*
    for (auto & kv : map<string, TGLViewer::ECameraType>{{"OrthoZOY", TGLViewer::kCameraOrthoZOY}, {"OrthoXOZ", TGLViewer::kCameraOrthoXOZ}, {"OrthoXOY", TGLViewer::kCameraOrthoXOY}}){
        viewer->CurrentCamera().Reset();
        viewer->SetCurrentCamera(kv.second);
        viewer->DoDraw();
        string pictureName = "pictures/view" + kv.first + ".png";
        cout << "Saving perspective '" << kv.first << "' to " << pictureName << endl;
        viewer->SavePictureWidth(pictureName.c_str(), 100);
    }
    */
}