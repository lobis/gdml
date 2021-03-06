void SaveGdmlToRoot(string filename="Setup.gdml", bool checkOverlaps=true){
    // first import it and see if there is something wrong with the file
    TGeoManager::Import(filename.c_str());
    // check for overlaps
    TGeoNode* node = gGeoManager->GetTopNode();
    if (checkOverlaps){
        node->CheckOverlaps(0.0001);
    }

    string outputFilename = filename + ".root";
    cout << "Writing geometry to: " << outputFilename << endl;
    TFile file(outputFilename.c_str(), "UPDATE");
    gGeoManager->Write("geometry");
    file.Close();

    TGeoManager::Import(filename.c_str());gGeoManager->GetTopVolume()->Draw("ogl");
    gGeoManager->SetVisLevel(5);
}
