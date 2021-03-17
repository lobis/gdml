import space.kscience.gdml.*

import java.io.File
import kotlin.io.*

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

val geometry = Gdml {

    // materials
    loadMaterialsFromUrl("https://raw.githubusercontent.com/rest-for-physics/materials/main/NIST.xml")
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
        // world
        val worldSize = 1000
        val worldMaterial = materials.composite("G4_AIR")
        val worldBox = solids.box(worldSize, worldSize, worldSize, "worldBox")
        // chamber
        val chamberBodySolid = solids.subtraction(
            solids.box(
                Chamber.SquareSide.mm,
                Chamber.SquareSide.mm,
                Chamber.Height.mm,
                "chamberBodyBaseSolid"
            ), solids.tube(Chamber.Diameter.mm * 0.5, Chamber.Height.mm, "chamberBodyHoleSolid"),
            "chamberBodySolid"
        )
        val chamberBodyVolume = volume(materialsMap["Copper"]!!, chamberBodySolid, "chamberBodyVolume")

        val chamberBackplateSolid = solids.box(
            Chamber.SquareSide.mm,
            Chamber.SquareSide.mm,
            Chamber.BackplateThickness.mm,
            "chamberBackplateSolid"
        )
        val chamberBackplateVolume = volume(materialsMap["Copper"]!!, chamberBackplateSolid, "chamberBackplateVolume")

        val chamberTeflonWallSolid =
            solids.tube(Chamber.Diameter.mm * 0.5, Chamber.Height.mm, "chamberTeflonWallSolid") {
                rmin = Chamber.Diameter.mm * 0.5 - Chamber.TeflonWallThickness.mm
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
            Chamber.SquareSide.mm * 0.5,
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
            solids.subtraction(cathodeTeflonDiskBaseSolid, cathodeCopperDiskSolid, "cathodeTeflonDiskSolid")

        val cathodeTeflonDiskVolume =
            volume(materialsMap["Teflon"]!!, cathodeTeflonDiskSolid, "cathodeTeflonDiskVolume") {}

        val cathodeWindowSolid =
            solids.tube(
                Chamber.CathodeTeflonDiskHoleRadius.mm,
                Chamber.CathodeWindowThickness.mm,
                "cathodeWindowSolid"
            )
        val cathodeWindowVolume = volume(materialsMap["Mylar"]!!, cathodeWindowSolid, "cathodeWindowVolume")

        val cathodeFillingBaseSolid = solids.tube(
            Chamber.CathodeTeflonDiskHoleRadius.mm,
            Chamber.CathodeTeflonDiskThickness.mm,
            "cathodeFillingBaseSolid"
        )
        val cathodeFillingSolid =
            solids.subtraction(cathodeFillingBaseSolid, cathodeCopperDiskSolid, "cathodeFillingSolid") {
                position = GdmlPosition(z = Chamber.Height.mm * 0.5 - Chamber.CathodeWindowThickness.mm * 0.5)
            }

        val cathodeFillingVolume = volume(materialsMap["Vacuum"]!!, cathodeFillingSolid, "cathodeFillingVolume") {}

        // cathode copper disk pattern
        val cathodePatternLineAux = solids.box(
            Chamber.CathodePatternLineWidth.mm,
            Chamber.CathodeCopperSupportInnerRadius.mm,
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


        var cathodeCopperDiskSolidAux: GdmlRef<GdmlUnion> = GdmlRef<GdmlUnion>("")

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
        val cathodeCopperDiskVolume = volume(materialsMap["Copper"]!!, cathodeCopperDiskFinal, "cathodeCopperDiskFinal")

        // gas

        val gasSolidOriginal = solids.tube(
            Chamber.Diameter.mm * 0.5 - Chamber.TeflonWallThickness.mm,
            Chamber.Height.mm, "gasSolidOriginal"
        )
        val gasSolidAux =
            solids.subtraction(gasSolidOriginal, copperReadoutSolid, "gasSolidAux") {
                position = GdmlPosition(z = -Chamber.Height.mm * 0.5 + Chamber.ReadoutCopperThickness.mm * 0.5)
            }
        val gasSolid =
            solids.subtraction(gasSolidAux, cathodeWindowSolid, "gasSolid") {
                position = GdmlPosition(z = Chamber.Height.mm * 0.5 - Chamber.CathodeWindowThickness.mm * 0.5)
                rotation = GdmlRotation(unit = AUnit.DEG, z = 45)
            }
        val gasVolume = volume(materialsMap["Gas"]!!, gasSolid, "gasVolume")

        val chamberVolume = assembly {
            /*
            physVolume(gasVolume) {
                name = "gas"
            }

            physVolume(chamberBackplateVolume) {
                name = "chamberBackplate"
                position {
                    z =
                        -Chamber.Height.mm * 0.5 - Chamber.ReadoutKaptonThickness.mm - Chamber.BackplateThickness.mm * 0.5
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
                    z = -Chamber.Height.mm * 0.5 - Chamber.ReadoutKaptonThickness.mm * 0.5
                }
            }

            physVolume(copperReadoutVolume) {
                name = "copperReadout"
                position {
                    z = -Chamber.Height.mm * 0.5 + Chamber.ReadoutCopperThickness.mm * 0.5
                }
                rotation {
                    unit = AUnit.DEG
                    z = 45
                }
            }

            physVolume(cathodeTeflonDiskVolume) {
                name = "cathodeTeflonDisk"
                position {
                    z = Chamber.Height.mm * 0.5 + Chamber.CathodeTeflonDiskThickness.mm * 0.5
                }
            }
            physVolume(cathodeWindowVolume) {
                name = "cathodeWindow"
                position {
                    z = Chamber.Height.mm * 0.5 - Chamber.CathodeWindowThickness.mm * 0.5
                }
            }
            physVolume(cathodeFillingVolume) {
                name = "cathodeFilling"
                position {
                    z = Chamber.Height.mm * 0.5 + Chamber.CathodeTeflonDiskThickness.mm * 0.5
                }
            }

            */
            physVolume(cathodeCopperDiskVolume) {
                name = "cathodeCopperDiskPattern"
                position {
                    z = Chamber.Height.mm * 0.5 + Chamber.CathodeCopperSupportThickness.mm * 0.5
                }
            }
        }

        // world setup
        world = volume(worldMaterial, worldBox, "world")
        {

            physVolume(chamberVolume) {
                name = "chamber"
            }
        }
    }
}


fun main() {

    val gdmlString = geometry.encodeToString()
    File("Setup.gdml").writeText(gdmlString)
}