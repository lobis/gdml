import space.kscience.gdml.*

import java.io.File

enum class Chamber(val mm: Double) {
    // Body + Backplate
    Height(30.0), Diameter(102.0),
    BackplateThickness(15.0), SquareSide(134.0),
    TeflonWallThickness(1.0),

    // Readout
    ReadoutKaptonThickness(0.5), ReadoutCopperThickness(0.2),
    ReadoutPlaneSide(60.0),

    // Cathode
    CathodeTeflonDiskHoleRadius(15.0), CathodeTeflonDiskThickness(5.0),
    CathodeCopperSupportOuterRadius(45.0), CathodeCopperSupportInnerRadius(8.5),
    CathodeCopperSupportThickness(1.0), CathodeWindowThickness(0.004),
    CathodePatternDiskRadius(4.25), CathodePatternLineWidth(0.3),
}

enum class DetectorPipe(val mm: Double) {
    TotalLength(491.0),

    // Outside
    ChamberFlangeThickness(14.0), ChamberFlangeRadius(Chamber.SquareSide.mm / 2),
    TelescopeFlangeThickness(18.0), TelescopeFlangeRadius(150.0 / 2),
    Section2of2Length(150.0 - TelescopeFlangeThickness.mm),
    Section1of2Length(TotalLength.mm - TelescopeFlangeThickness.mm - ChamberFlangeThickness.mm - Section2of2Length.mm),
    OuterRadius1(92.0 / 2), OuterRadius2(108.0 / 2),
    Union1Z(ChamberFlangeThickness.mm / 2 + Section1of2Length.mm / 2),
    Union2Z(Union1Z.mm + Section1of2Length.mm / 2 + Section2of2Length.mm / 2),
    Union3Z(Union2Z.mm + Section2of2Length.mm / 2 + TelescopeFlangeThickness.mm / 2),

    // Inside
    InsideSection1of3Radius(43.0 / 2), InsideSection2of3Radius(68.0 / 2), InsideSection3of3Radius(85.0 / 2),
    InsideSectionTelescopeRadius(108.0 / 2),
    InsideCone1of3Length(21.65), InsideCone2of3Length(14.72), InsideCone3of3Length(9.0),
    InsideSection3of3Length(115.0 - InsideCone3of3Length.mm),
    InsideSection2of3Length(290.0 - InsideSection3of3Length.mm - InsideCone3of3Length.mm - InsideCone2of3Length.mm),
    InsideSection1of3Length(201.0 - InsideCone1of3Length.mm),
    InsideUnion1Z(InsideSection1of3Length.mm / 2 + InsideCone1of3Length.mm / 2),
    InsideUnion2Z(InsideUnion1Z.mm + InsideCone1of3Length.mm / 2 + InsideSection2of3Length.mm / 2),
    InsideUnion3Z(InsideUnion2Z.mm + InsideSection2of3Length.mm / 2 + InsideCone2of3Length.mm / 2),
    InsideUnion4Z(InsideUnion3Z.mm + InsideCone2of3Length.mm / 2 + InsideSection3of3Length.mm / 2),
    InsideUnion5Z(InsideUnion4Z.mm + InsideSection3of3Length.mm / 2 + InsideCone3of3Length.mm / 2),
    FillingOffsetWithPipe(InsideSection1of3Length.mm / 2 - ChamberFlangeThickness.mm / 2),

    // World
    ZinWorld(ChamberFlangeThickness.mm / 2 + Chamber.Height.mm / 2 + Chamber.CathodeTeflonDiskThickness.mm),
}

enum class Shielding(val mm: Double) {
    SizeXY(590.0), SizeZ(540.0),
    ShaftShortSideX(194.0), ShaftShortSideY(170.0),
    ShaftLongSide(340.0),
    DetectorToShieldingSeparation(-60.0),
    OffsetZ(DetectorToShieldingSeparation.mm + Chamber.Height.mm / 2 + Chamber.ReadoutKaptonThickness.mm + Chamber.BackplateThickness.mm),
}

val geometry = Gdml {
    // materials
    loadMaterialsFromUrl("https://raw.githubusercontent.com/rest-for-physics/materials/main/materials.xml")
    val materialsMap = mapOf(
        "Gas" to materials.composite("G4_Ar"),
        "Vacuum" to materials.composite("G4_Galactic"),
        "Copper" to materials.composite("G4_Cu"),
        "Lead" to materials.composite("G4_Pb"),
        "Teflon" to materials.composite("G4_TEFLON"),
        "Kapton" to materials.composite("G4_KAPTON"),
        "Mylar" to materials.composite("G4_MYLAR"),
    )

    structure {

        fun chamberVolume(): GdmlRef<GdmlAssembly> {
            val chamberBodySolid = solids.subtraction(
                solids.box(
                    Chamber.SquareSide.mm,
                    Chamber.SquareSide.mm,
                    Chamber.Height.mm,
                    "chamberBodyBaseSolid"
                ), solids.tube(Chamber.Diameter.mm / 2, Chamber.Height.mm, "chamberBodyHoleSolid"),
                "chamberBodySolid"
            )
            val chamberBodyVolume = volume(materialsMap["Copper"]!!, chamberBodySolid, "chamberBodyVolume")

            val chamberBackplateSolid = solids.box(
                Chamber.SquareSide.mm,
                Chamber.SquareSide.mm,
                Chamber.BackplateThickness.mm,
                "chamberBackplateSolid"
            )
            val chamberBackplateVolume =
                volume(materialsMap["Copper"]!!, chamberBackplateSolid, "chamberBackplateVolume")

            val chamberTeflonWallSolid =
                solids.tube(Chamber.Diameter.mm / 2, Chamber.Height.mm, "chamberTeflonWallSolid") {
                    rmin = Chamber.Diameter.mm / 2 - Chamber.TeflonWallThickness.mm
                }
            val chamberTeflonWallVolume =
                volume(materialsMap["Teflon"]!!, chamberTeflonWallSolid, "chamberTeflonWallVolume")
            // readout
            val kaptonReadoutSolid = solids.box(
                Chamber.SquareSide.mm, Chamber.SquareSide.mm, Chamber.ReadoutKaptonThickness.mm, "kaptonReadoutSolid"
            )
            val kaptonReadoutVolume = volume(materialsMap["Kapton"]!!, kaptonReadoutSolid, "kaptonReadoutVolume")

            val copperReadoutSolid =
                solids.box(
                    Chamber.ReadoutPlaneSide.mm,
                    Chamber.ReadoutPlaneSide.mm,
                    Chamber.ReadoutCopperThickness.mm,
                    "copperReadoutSolid"
                )
            val copperReadoutVolume = volume(materialsMap["Kapton"]!!, copperReadoutSolid, "copperReadoutVolume")

            // cathode
            val cathodeTeflonDiskBaseSolid = solids.tube(
                Chamber.SquareSide.mm / 2,
                Chamber.CathodeTeflonDiskThickness.mm,
                "cathodeTeflonDiskBaseSolid"
            ) {
                rmin = Chamber.CathodeTeflonDiskHoleRadius.mm
            }
            val cathodeCopperDiskSolid = solids.tube(
                Chamber.CathodeCopperSupportOuterRadius.mm,
                Chamber.CathodeCopperSupportThickness.mm,
                "cathodeCopperDiskSolid"
            ) {
                rmin = Chamber.CathodeCopperSupportInnerRadius.mm
            }
            val cathodeTeflonDiskSolid =
                solids.subtraction(cathodeTeflonDiskBaseSolid, cathodeCopperDiskSolid, "cathodeTeflonDiskSolid") {
                    position =
                        GdmlPosition(z = -Chamber.CathodeTeflonDiskThickness.mm / 2 + Chamber.CathodeCopperSupportThickness.mm / 2)
                }

            val cathodeTeflonDiskVolume =
                volume(materialsMap["Teflon"]!!, cathodeTeflonDiskSolid, "cathodeTeflonDiskVolume") {}

            val cathodeWindowSolid =
                solids.tube(
                    Chamber.CathodeTeflonDiskHoleRadius.mm,
                    Chamber.CathodeWindowThickness.mm,
                    "cathodeWindowSolid"
                )
            val cathodeWindowVolume = volume(materialsMap["Mylar"]!!, cathodeWindowSolid, "cathodeWindowVolume")

            // cathode copper disk pattern
            val cathodePatternLineAux = solids.box(
                Chamber.CathodePatternLineWidth.mm,
                Chamber.CathodeCopperSupportInnerRadius.mm * 2,
                Chamber.CathodeCopperSupportThickness.mm,
                "cathodePatternLineAux"
            )
            val cathodePatternCentralHole = solids.tube(
                Chamber.CathodePatternDiskRadius.mm,
                Chamber.CathodeCopperSupportThickness.mm * 1.1, "cathodePatternCentralHole"
            )
            val cathodePatternLine =
                solids.subtraction(cathodePatternLineAux, cathodePatternCentralHole, "cathodePatternLine")

            val cathodePatternDisk = solids.tube(
                Chamber.CathodePatternDiskRadius.mm,
                Chamber.CathodeCopperSupportThickness.mm, "cathodePatternDisk"
            ) { rmin = Chamber.CathodePatternDiskRadius.mm - Chamber.CathodePatternLineWidth.mm }


            var cathodeCopperDiskSolidAux: GdmlRef<GdmlUnion> = GdmlRef("")

            for (i in 0..3) {
                cathodeCopperDiskSolidAux =
                    solids.union(
                        if (i > 0) cathodeCopperDiskSolidAux else cathodeCopperDiskSolid,
                        cathodePatternLine,
                        "cathodeCopperDiskSolidAux$i"
                    ) {
                        rotation = GdmlRotation(
                            unit = AUnit.DEG, x = 0, y = 0, z = 45 * i
                        )
                    }
            }

            val cathodeCopperDiskFinal =
                solids.union(cathodeCopperDiskSolidAux, cathodePatternDisk, "cathodeCopperDiskFinal")
            val cathodeCopperDiskVolume =
                volume(materialsMap["Copper"]!!, cathodeCopperDiskFinal, "cathodeCopperDiskFinal")

            val cathodeFillingBaseSolid = solids.tube(
                Chamber.CathodeTeflonDiskHoleRadius.mm,
                Chamber.CathodeTeflonDiskThickness.mm,
                "cathodeFillingBaseSolid"
            )
            val cathodeFillingSolid =
                solids.subtraction(cathodeFillingBaseSolid, cathodeCopperDiskFinal, "cathodeFillingSolid") {
                    position =
                        GdmlPosition(z = -Chamber.CathodeTeflonDiskThickness.mm / 2 + Chamber.CathodeCopperSupportThickness.mm / 2)
                }
            val cathodeFillingVolume = volume(materialsMap["Vacuum"]!!, cathodeFillingSolid, "cathodeFillingVolume") {}

            // gas
            val gasSolidOriginal = solids.tube(
                Chamber.Diameter.mm / 2 - Chamber.TeflonWallThickness.mm,
                Chamber.Height.mm, "gasSolidOriginal"
            )
            val gasSolidAux =
                solids.subtraction(gasSolidOriginal, copperReadoutSolid, "gasSolidAux") {
                    position = GdmlPosition(z = -Chamber.Height.mm / 2 + Chamber.ReadoutCopperThickness.mm / 2)
                    rotation = GdmlRotation(unit = AUnit.DEG, z = 45)
                }
            val gasSolid =
                solids.subtraction(gasSolidAux, cathodeWindowSolid, "gasSolid") {
                    position = GdmlPosition(z = Chamber.Height.mm / 2 - Chamber.CathodeWindowThickness.mm / 2)
                }
            val gasVolume = volume(materialsMap["Gas"]!!, gasSolid, "gasVolume")

            return assembly {
                physVolume(gasVolume) {
                    name = "gas"
                }
                physVolume(chamberBackplateVolume) {
                    name = "chamberBackplate"
                    position {
                        z =
                            -Chamber.Height.mm / 2 - Chamber.ReadoutKaptonThickness.mm - Chamber.BackplateThickness.mm / 2
                    }
                }
                physVolume(chamberBodyVolume) {
                    name = "chamberBody"
                }
                physVolume(chamberTeflonWallVolume) {
                    name = "chamberTeflonWall"
                }
                physVolume(kaptonReadoutVolume) {
                    name = "kaptonReadout"
                    position {
                        z = -Chamber.Height.mm / 2 - Chamber.ReadoutKaptonThickness.mm / 2
                    }
                }
                physVolume(copperReadoutVolume) {
                    name = "copperReadout"
                    position {
                        z = -Chamber.Height.mm / 2 + Chamber.ReadoutCopperThickness.mm / 2
                    }
                    rotation {
                        unit = AUnit.DEG
                        z = 45
                    }
                }
                physVolume(cathodeWindowVolume) {
                    name = "cathodeWindow"
                    position {
                        z = Chamber.Height.mm / 2 - Chamber.CathodeWindowThickness.mm / 2
                    }
                }

                physVolume(cathodeTeflonDiskVolume) {
                    name = "cathodeTeflonDisk"
                    position {
                        z = Chamber.Height.mm / 2 + Chamber.CathodeTeflonDiskThickness.mm / 2
                    }
                }
                physVolume(cathodeFillingVolume) {
                    name = "cathodeFilling"
                    position {
                        z = Chamber.Height.mm / 2 + Chamber.CathodeTeflonDiskThickness.mm / 2
                    }
                }
                physVolume(cathodeCopperDiskVolume) {
                    name = "cathodeCopperDiskPattern"
                    position {
                        z = Chamber.Height.mm / 2 + Chamber.CathodeCopperSupportThickness.mm / 2
                    }
                }
            }
        };
        val chamberVolume = chamberVolume()

        fun detectorPipeVolume(): GdmlRef<GdmlAssembly> {

            val detectorPipeChamberFlangeSolid = solids.tube(
                DetectorPipe.ChamberFlangeRadius.mm,
                DetectorPipe.ChamberFlangeThickness.mm,
                "detectorPipeChamberFlangeSolid"
            )
            val detectorPipeTelescopeFlangeSolid = solids.tube(
                DetectorPipe.TelescopeFlangeRadius.mm,
                DetectorPipe.TelescopeFlangeThickness.mm,
                "detectorPipeTelescopeFlangeSolid"
            )
            val detectorPipeSection1of2Solid =
                solids.tube(
                    DetectorPipe.OuterRadius1.mm,
                    DetectorPipe.Section1of2Length.mm,
                    "detectorPipeSection1of2Solid"
                )
            val detectorPipeSection2of2Solid =
                solids.tube(
                    DetectorPipe.OuterRadius2.mm,
                    DetectorPipe.Section2of2Length.mm,
                    "detectorPipeSection2of2Solid"
                )
            val detectorPipeAux1 =
                solids.union(detectorPipeChamberFlangeSolid, detectorPipeSection1of2Solid, "detectorPipeAux1")
                {
                    position = GdmlPosition(z = DetectorPipe.Union1Z.mm)
                }
            val detectorPipeAux2 =
                solids.union(detectorPipeAux1, detectorPipeSection2of2Solid, "detectorPipeAux2")
                {
                    position = GdmlPosition(z = DetectorPipe.Union2Z.mm)
                }
            val detectorPipeNotEmpty =
                solids.union(detectorPipeAux2, detectorPipeTelescopeFlangeSolid, "detectorPipeNotEmpty")
                {
                    position = GdmlPosition(z = DetectorPipe.Union3Z.mm)
                }
            val detectorPipeInside1of3Solid = solids.tube(
                DetectorPipe.InsideSection1of3Radius.mm,
                DetectorPipe.InsideSection1of3Length.mm,
                "detectorPipeInside1of3Solid"
            )
            val detectorPipeInside2of3Solid = solids.tube(
                DetectorPipe.InsideSection2of3Radius.mm,
                DetectorPipe.InsideSection2of3Length.mm,
                "detectorPipeInside2of3Solid"
            )
            val detectorPipeInside3of3Solid = solids.tube(
                DetectorPipe.InsideSection3of3Radius.mm,
                DetectorPipe.InsideSection3of3Length.mm,
                "detectorPipeInside3of3Solid"
            )
            val detectorPipeInsideCone1of3Solid = solids.cone(
                DetectorPipe.InsideCone1of3Length.mm,
                DetectorPipe.InsideSection1of3Radius.mm, DetectorPipe.InsideSection2of3Radius.mm,
                "detectorPipeInsideCone1of3Solid"
            )
            val detectorPipeInsideCone2of3Solid = solids.cone(
                DetectorPipe.InsideCone2of3Length.mm,
                DetectorPipe.InsideSection2of3Radius.mm, DetectorPipe.InsideSection3of3Radius.mm,
                "detectorPipeInsideCone2of3Solid"
            )
            val detectorPipeInsideCone3of3Solid = solids.cone(
                DetectorPipe.InsideCone3of3Length.mm,
                DetectorPipe.InsideSection3of3Radius.mm, DetectorPipe.InsideSectionTelescopeRadius.mm,
                "detectorPipeInsideCone3of3Solid"
            )
            val detectorPipeInsideAux1 =
                solids.union(detectorPipeInside1of3Solid, detectorPipeInsideCone1of3Solid, "detectorPipeInsideAux1") {
                    position = GdmlPosition(z = DetectorPipe.InsideUnion1Z.mm)
                }
            val detectorPipeInsideAux2 =
                solids.union(detectorPipeInsideAux1, detectorPipeInside2of3Solid, "detectorPipeInsideAux2") {
                    position = GdmlPosition(z = DetectorPipe.InsideUnion2Z.mm)
                }
            val detectorPipeInsideAux3 =
                solids.union(detectorPipeInsideAux2, detectorPipeInsideCone2of3Solid, "detectorPipeInsideAux3") {
                    position = GdmlPosition(z = DetectorPipe.InsideUnion3Z.mm)
                }
            val detectorPipeInsideAux4 =
                solids.union(detectorPipeInsideAux3, detectorPipeInside3of3Solid, "detectorPipeInsideAux4") {
                    position = GdmlPosition(
                        z = DetectorPipe.InsideUnion4Z.mm
                    )
                }
            val detectorPipeInside =
                solids.union(detectorPipeInsideAux4, detectorPipeInsideCone3of3Solid, "detectorPipeInside") {
                    position = GdmlPosition(z = DetectorPipe.InsideUnion5Z.mm)
                }
            val detectorPipeSolid = solids.subtraction(detectorPipeNotEmpty, detectorPipeInside, "detectorPipeSolid") {
                position =
                    GdmlPosition(z = DetectorPipe.InsideSection1of3Length.mm / 2 - DetectorPipe.ChamberFlangeThickness.mm / 2)
            }
            val detectorPipeVolume = volume(materialsMap["Copper"]!!, detectorPipeSolid, "detectorPipeVolume")
            val detectorPipeFillingVolume =
                volume(materialsMap["Vacuum"]!!, detectorPipeInside, "detectorPipeFillingVolume")

            return assembly {
                physVolume(detectorPipeVolume) {
                    name = "detectorPipe"
                }
                physVolume(detectorPipeFillingVolume) {
                    name = "detectorPipeFilling"
                    position {
                        z = DetectorPipe.FillingOffsetWithPipe.mm
                    }
                }
            }
        };
        val detectorPipeVolume = detectorPipeVolume()

        fun shieldingVolume(): GdmlRef<GdmlAssembly> {
            val leadBoxSolid = solids.box(Shielding.SizeXY.mm, Shielding.SizeXY.mm, Shielding.SizeZ.mm, "leadBoxSolid")
            val leadBoxShaftSolid =
                solids.box(
                    Shielding.ShaftShortSideX.mm,
                    Shielding.ShaftShortSideY.mm,
                    Shielding.ShaftLongSide.mm,
                    "leadBoxShaftSolid"
                )
            val leadBoxWithShaftSolid = solids.subtraction(leadBoxSolid, leadBoxShaftSolid, "leadBoxWithShaftSolid") {
                position = GdmlPosition(z = Shielding.SizeZ.mm / 2 - Shielding.ShaftLongSide.mm / 2)
            }
            val leadShieldingVolume = volume(materialsMap["Lead"]!!, leadBoxWithShaftSolid, "ShieldingVolume")

            return assembly {
                physVolume(leadShieldingVolume) {
                    name = "shielding20cm"
                    position {
                        z = -Shielding.OffsetZ.mm
                    }
                }
            }
        };
        val shieldingVolume = shieldingVolume()

        val worldSize = 4000
        val worldBox = solids.box(worldSize, worldSize, worldSize, "worldBox")
        world = volume(materials.composite("G4_AIR"), worldBox, "world")
        {
            physVolume(chamberVolume) {
                name = "Chamber"
            }
            physVolume(detectorPipeVolume) {
                name = "DetectorPipe"
                position {
                    z = DetectorPipe.ZinWorld.mm
                }
            }
            physVolume(shieldingVolume) {
                name = "Shielding"
            }
        }
    }
}

fun main() {
    File("Setup.gdml").writeText(geometry.encodeToString())
}